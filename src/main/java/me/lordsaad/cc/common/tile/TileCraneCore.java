package me.lordsaad.cc.common.tile;

import com.teamwizardry.librarianlib.common.base.block.TileMod;
import com.teamwizardry.librarianlib.common.util.autoregister.TileRegister;
import com.teamwizardry.librarianlib.common.util.math.Vec2d;
import com.teamwizardry.librarianlib.common.util.saving.Save;
import com.teamwizardry.librarianlib.common.util.saving.SaveMethodGetter;
import com.teamwizardry.librarianlib.common.util.saving.SaveMethodSetter;
import kotlin.Pair;
import me.lordsaad.cc.api.PosUtils;
import me.lordsaad.cc.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;

/**
 * Created by LordSaad.
 */
@TileRegister("crane_core")
public class TileCraneCore extends TileMod implements ITickable {

	public HashSet<Pair<IBlockState, BlockPos>> queue = new HashSet<>();
	@Save
	public float prevYaw = 0;
	@Save
	public float currentYaw = 0;
	@Save
	public float destYaw = 0;
	@Nullable
	@Save
	public EnumFacing originalDirection;
	@Save
	public BlockPos originalArmPos;
	@Save
	public boolean transitionArm = false, transitionArmToOrigin;
	@Save
	public int armLength = 0;
	@Nullable
	public Pair<IBlockState, BlockPos> nextPair = null;
	@Save
	public long worldTime;

	@SaveMethodGetter(saveName = "nextPair")
	public NBTTagCompound nextPairGetter() {
		NBTTagCompound nbt = new NBTTagCompound();
		if (nextPair == null) return nbt;

		NBTUtil.writeBlockState(nbt, nextPair.getFirst()).setTag("block_pos", NBTUtil.createPosTag(nextPair.getSecond()));
		return nbt;
	}

	@SaveMethodSetter(saveName = "nextPair")
	public void nextPairSetter(NBTTagCompound nbt) {
		nextPair = new Pair<>(NBTUtil.readBlockState(nbt), NBTUtil.getPosFromTag(nbt.getCompoundTag("block_pos")));
	}

	@SaveMethodGetter(saveName = "queue")
	public NBTTagCompound queueGetter() {
		NBTTagCompound nbt = new NBTTagCompound();
		if (queue.isEmpty()) return nbt;

		NBTTagList list = new NBTTagList();
		for (Pair<IBlockState, BlockPos> pair : queue) {
			NBTTagCompound compound = new NBTTagCompound();
			NBTUtil.writeBlockState(compound, pair.getFirst());
			compound.setTag("block_pos", NBTUtil.createPosTag(pos));
			list.appendTag(compound);
		}
		nbt.setTag("list", list);
		return nbt;
	}

	@SaveMethodSetter(saveName = "queue")
	public void queueSetter(NBTTagCompound nbt) {
		NBTTagList list = nbt.getTagList("list", Constants.NBT.TAG_COMPOUND);
		for (int q = 0; q < list.tagCount(); q++) {
			NBTTagCompound compound = list.getCompoundTagAt(q);
			IBlockState state = NBTUtil.readBlockState(compound);
			BlockPos pos = NBTUtil.getPosFromTag(nbt.getCompoundTag("block_pos"));
			queue.add(new Pair<>(state, pos));
		}
	}

	@Override
	public void update() {
		if (transitionArm) {
			double transitionTimeMax = Math.max(10, Math.min(Math.abs((prevYaw - destYaw) / 2.0), 35));
			double worldTimeTransition = (world.getTotalWorldTime() - worldTime);

			float yaw;
			if (worldTimeTransition < transitionTimeMax) {
				if (Math.round(destYaw) > Math.round(prevYaw))
					yaw = -((destYaw - prevYaw) / 2) * MathHelper.cos((float) (worldTimeTransition * Math.PI / transitionTimeMax)) + (destYaw + prevYaw) / 2;
				else
					yaw = ((prevYaw - destYaw) / 2) * MathHelper.cos((float) (worldTimeTransition * Math.PI / transitionTimeMax)) + (destYaw + prevYaw) / 2;
				currentYaw = yaw;
			} else {
				currentYaw = destYaw;
				transitionArm = false;

				if (queue.isEmpty() && originalDirection != null) {
					for (int i = 1; i < armLength; i++) {
						BlockPos armPos = originalArmPos.offset(originalDirection, i);
						world.setBlockState(armPos, ModBlocks.CRANE_BASE.getDefaultState(), 3);
					}
				}
			}
			markDirty();
		} else if (!queue.isEmpty()) {
			nextPair = queue.iterator().next();
			worldTime = world.getTotalWorldTime();
			transitionArm = true;

			if (nextPair == null) return;

			queue.remove(nextPair);

			HashSet<BlockPos> arm = PosUtils.getCraneHorizontalPole(world, pos);
			Pair<BlockPos, EnumFacing> pair = PosUtils.getHorizontalOriginAndDirection(world, pos);

			if (arm != null) armLength = arm.size();
			if (pair != null) {
				originalArmPos = pair.getFirst();
				originalDirection = pair.getSecond();

				Vec3d tempSubOrigin = new Vec3d(pos.offset(pair.getSecond())).addVector(0.5, 0.5, 0.5);
				Vec3d tempSubTo = new Vec3d(nextPair.getSecond()).addVector(0.5, 0.5, 0.5);
				Vec2d origin = new Vec2d(pos.getX(), pos.getZ()).add(0.5, 0.5).sub(tempSubOrigin.xCoord, tempSubOrigin.zCoord).normalize();
				Vec2d to = new Vec2d(pos.getX(), pos.getZ()).add(0.5, 0.5).sub(tempSubTo.xCoord, tempSubTo.zCoord).normalize();
				double dot = origin.dot(to);
				double det = (origin.getX() * to.getY()) - (origin.getY() * to.getX());

				double angle = 360 - Math.toDegrees(Math.atan2(det, dot));//Math.toDegrees(Math.acos(origin.dotProduct(to) / (origin.lengthVector() * to.lengthVector())));
				//if (Double.isNaN(angle)) angle = 0;
				destYaw = (float) angle;
				Minecraft.getMinecraft().player.sendChatMessage(angle + "");
			}
			prevYaw = currentYaw;

			if (arm != null)
				for (BlockPos blocks : arm) {
					if (blocks.toLong() == pos.toLong()) continue;
					world.setBlockToAir(blocks);
				}

			markDirty();
		}
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
}
