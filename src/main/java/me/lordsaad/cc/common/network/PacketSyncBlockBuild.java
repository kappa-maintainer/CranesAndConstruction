package me.lordsaad.cc.common.network;

import com.mojang.authlib.GameProfile;
import com.teamwizardry.librarianlib.features.math.Vec2d;
import com.teamwizardry.librarianlib.features.network.PacketBase;
import com.teamwizardry.librarianlib.features.saving.Save;
import kotlin.Pair;
import me.lordsaad.cc.common.tile.TileCraneCore;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

/**
 * Created by LordSaad.
 */
public class PacketSyncBlockBuild extends PacketBase {

	private static final GameProfile profile = new GameProfile(UUID.fromString("0e6ea501-d440-4069-9db4-5ec3f2ff6ae0"), "crane");

	@Save
	private BlockPos crane;
	@Save
	private int slot;
	@Save
	private BlockPos destination;
	@Save
	private int width;

	public PacketSyncBlockBuild() {
	}

	public PacketSyncBlockBuild(BlockPos crane, int slot, BlockPos destination, int width) {
		this.crane = crane;
		this.slot = slot;
		this.destination = destination;
		this.width = width;
	}

	@Override
	public void handle(MessageContext messageContext) {
		World world = messageContext.getServerHandler().playerEntity.world;

		//int width;
		//HashSet<BlockPos> horizontal = PosUtils.getCraneHorizontalPole(world, crane);
		//if (horizontal == null) return;
		//if (horizontal.isEmpty()) return;
		//else width = horizontal.size() - 1;
//
		//Minecraft.getMinecraft().player.sendChatMessage(width + " - " + ((int)destination.getDistance(crane.getX(), crane.getY(), crane.getZ())));
		double dist = new Vec2d(destination.getX(), destination.getZ()).add(0.5, 0.5).sub(new Vec2d(crane.getX(), crane.getZ()).add(0.5, 0.5)).length();

		if (dist > width) return;

		if (!world.isBlockLoaded(destination)) return;

		TileCraneCore core = (TileCraneCore) world.getTileEntity(crane);
		if (core == null) return;

		ItemStack itemBlock = messageContext.getServerHandler().playerEntity.inventory.getStackInSlot(slot);
		BlockPos temp = new BlockPos(crane.getX(), 250, crane.getZ());

		FakePlayer player = new FakePlayer((WorldServer) world, profile);
		player.rotationYaw = EnumFacing.DOWN.getHorizontalAngle();
		player.rotationPitch = 90f;
		player.posX = destination.getX() + 0.5;
		player.posY = destination.getY() + 0.5 - player.eyeHeight;
		player.posZ = destination.getZ() + 0.5;
		player.setHeldItem(EnumHand.MAIN_HAND, itemBlock);

		EnumActionResult result = itemBlock.onItemUse(player, world, temp, EnumHand.MAIN_HAND, EnumFacing.UP, 0f, 0f, 0f);
		if (result == EnumActionResult.PASS) return;

		if (world.isAirBlock(temp)) return;
		IBlockState state = world.getBlockState(temp);
		world.setBlockToAir(temp);

		if (!messageContext.getServerHandler().playerEntity.isCreative())
			itemBlock.shrink(1);

		core.queue.add(new Pair<>(state, destination));
		core.markDirty();
	}
}
