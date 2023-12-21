package me.lordsaad.cc.common.tile;

import com.teamwizardry.librarianlib.features.autoregister.TileRegister;
import com.teamwizardry.librarianlib.features.base.block.tile.TileMod;
import com.teamwizardry.librarianlib.features.math.Vec2d;
import com.teamwizardry.librarianlib.features.saving.Save;
import com.teamwizardry.librarianlib.features.saving.SaveMethodGetter;
import com.teamwizardry.librarianlib.features.saving.SaveMethodSetter;
import kotlin.Pair;
import me.lordsaad.cc.CCMain;
import me.lordsaad.cc.api.CraneManager;
import me.lordsaad.cc.api.PosUtils;
import me.lordsaad.cc.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by LordSaad.
 */
@TileRegister("crane_core")
public class TileCraneCore extends TileMod implements ITickable {

	public Deque<Pair<IBlockState, BlockPos>> queue = new ArrayDeque<>(256);
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
	@Deprecated
	public BlockPos originalArmPos;
	@Save
	public int armHeight;
	@Save
	public boolean transitionArm = false, transitionArmToOrigin;
	@Save
	public int armLength = 0;
	@Nullable
	public Pair<IBlockState, BlockPos> nextPair = null;
	@Save
	public long worldTime;
	// Handle
	@Save
	public BlockPos handleTo;
	public IBlockState craneArmSample = ModBlocks.SCAFFOLDING.getDefaultState();

	@SaveMethodGetter(saveName = "craneArmSample_saver")
	public NBTTagCompound craneArmSampleGetter() {
		NBTTagCompound nbt = new NBTTagCompound();
		if (craneArmSample == null) return nbt;
		NBTUtil.writeBlockState(nbt, craneArmSample);
		return nbt;
	}

	@SaveMethodSetter(saveName = "craneArmSample_saver")
	public void craneArmSampleSetter(NBTTagCompound nbt) {
		craneArmSample = NBTUtil.readBlockState(nbt);
	}

	@SaveMethodGetter(saveName = "nextPair_saver")
	public NBTTagCompound nextPairGetter() {
		NBTTagCompound nbt = new NBTTagCompound();
		if (nextPair == null) return nbt;

		NBTUtil.writeBlockState(nbt, nextPair.getFirst()).setTag("block_pos", NBTUtil.createPosTag(nextPair.getSecond()));
		return nbt;
	}

	@SaveMethodSetter(saveName = "nextPair_saver")
	public void nextPairSetter(NBTTagCompound nbt) {
		nextPair = new Pair<>(NBTUtil.readBlockState(nbt), NBTUtil.getPosFromTag(nbt.getCompoundTag("block_pos")));
	}

	@SaveMethodGetter(saveName = "queue_saver")
	public NBTTagCompound queueGetter() {
		NBTTagCompound nbt = new NBTTagCompound();
		if (queue.isEmpty()) return nbt;

		NBTTagList list = new NBTTagList();
		for (Pair<IBlockState, BlockPos> pair : queue) {
			NBTTagCompound compound = new NBTTagCompound();
			NBTUtil.writeBlockState(compound, pair.getFirst());
			compound.setTag("block_pos", NBTUtil.createPosTag(pair.getSecond()));
			list.appendTag(compound);
		}
		nbt.setTag("list", list);
		return nbt;
	}

	@SaveMethodSetter(saveName = "queue_saver")
	public void queueSetter(NBTTagCompound nbt) {
		queue.clear();
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
		if (!queue.isEmpty())CCMain.LOGGER.info("QUEUE NOT EMPTY1: " + queue.peek());
		if (transitionArm || transitionArmToOrigin) CCMain.LOGGER.info("trans: " + transitionArm +", transo: " + transitionArmToOrigin);
		if (transitionArm) {
			if (Float.isNaN(destYaw)) destYaw = 0;
			double transitionTimeMax = Math.max(10, Math.min(Math.abs((prevYaw - destYaw) / 2.0), 20));

			int worldTimeTransition = (int) (world.getTotalWorldTime() - worldTime);
			CCMain.LOGGER.info(worldTimeTransition + " : " + transitionTimeMax);
			CCMain.LOGGER.info(prevYaw + ":" + destYaw);

			if (worldTimeTransition >= transitionTimeMax) {
				currentYaw = destYaw;
				transitionArm = false;
				CCMain.LOGGER.info("transition complete");
				if (nextPair != null) {
					EntityFallingBlock block = new EntityFallingBlock(world, nextPair.getSecond().getX() + 0.5, nextPair.getSecond().getY(), nextPair.getSecond().getZ() + 0.5, nextPair.getFirst());
					block.fallTime = 2;
					world.spawnEntity(block);
				}

				if (originalDirection != null) {
					prevYaw = currentYaw;
					worldTime = world.getTotalWorldTime();
					if (queue.isEmpty()) {
						destYaw = 0;
						transitionArmToOrigin = true;
					}
				}
				markDirty();
				world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), Constants.BlockFlags.DEFAULT);
			}

		} else if (transitionArmToOrigin) {
			double transitionTimeMax = Math.max(10, Math.min(Math.abs((prevYaw - destYaw) / 2.0), 20));
			double worldTimeTransition = (world.getTotalWorldTime() - worldTime);

			if (worldTimeTransition >= transitionTimeMax) {
				prevYaw = currentYaw = destYaw = 0;
				worldTime = world.getTotalWorldTime();
				transitionArmToOrigin = false;
				nextPair = null;

				if (queue.isEmpty() && originalDirection != null) {
					for (int i = 1; i < armLength; i++) {
						BlockPos armPos = new BlockPos(pos.getX(), armHeight, pos.getY()).offset(originalDirection, i);
						world.setBlockState(armPos, ModBlocks.SCAFFOLDING.getDefaultState());
					}
				}
				markDirty();
				world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), Constants.BlockFlags.DEFAULT);
			}
		} else if (!queue.isEmpty()) {
			nextPair = queue.pop();
			CCMain.LOGGER.info("QUEUE NOT EMPTY2: " + nextPair);
			if (nextPair == null) return;

			CraneManager manager = new CraneManager(world, pos);
			if (manager.width == 0) return;
			if (manager.height == 0) return;

			transitionArm = true;
			armLength = manager.width;
			armHeight = manager.armBlock.getY();
			originalDirection = manager.direction;
			if (originalArmPos == null) originalArmPos = new BlockPos(pos.getX(), armHeight, pos.getY()).offset(originalDirection, 0);
			worldTime = world.getTotalWorldTime();

			BlockPos nextPos = new BlockPos(nextPair.getSecond().getX(), originalArmPos.getY() - 1, nextPair.getSecond().getZ());
			handleTo = pos.offset(originalDirection, (int) pos.getDistance(nextPos.getX(), nextPos.getY(), nextPos.getZ()));

			Vec3d from3d = new Vec3d(pos).subtract(new Vec3d(pos.offset(originalDirection)));
			Vec3d to3d = new Vec3d(pos).subtract(new Vec3d(nextPair.getSecond()));
			Vec2d from = new Vec2d(from3d.x, from3d.z).normalize();
			Vec2d to = new Vec2d(to3d.x, to3d.z).normalize();
			double angle1 = Math.acos(from.getX()) * (from.getY() < 0 ? -1 : 1);
			double angle2 = Math.acos(to.getX()) * (to.getY() < 0 ? -1 : 1);
			double angle = Math.toDegrees(angle1 - angle2);
			destYaw = (float) angle;

			if (!manager.arm.isEmpty())
				for (BlockPos blocks : manager.arm)
					if (!PosUtils.isPosInSet(blocks, manager.pole))
						world.setBlockToAir(blocks);

			markDirty();
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		}
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
}
