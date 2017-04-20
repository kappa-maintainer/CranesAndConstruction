package me.lordsaad.cc.common;

import me.lordsaad.cc.api.ILadder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by LordSaad.
 */
public class CommonEventHandler {
	public static final CommonEventHandler INSTANCE = new CommonEventHandler();

	private CommonEventHandler() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		Entity entity = event.getEntity();

		if (entity instanceof EntityPlayer)
			if (((EntityPlayer) entity).isSpectator()
					|| ((EntityPlayer) entity).isElytraFlying()
					|| ((EntityPlayer) entity).capabilities.isFlying) return;

		BlockPos collidedLadder = getCollidedILadder(entity);
		if (collidedLadder == null) return;

		double ladderClimbSpeed = entity.isSneaking() ? 0.0 : 0.15;
		double ladderFallSpeed = entity.isSneaking() ? 0.0 : 0.3;

		if (entity.isCollidedHorizontally) {
			if (entity.motionY < ladderClimbSpeed) {
				entity.motionY = ladderClimbSpeed;
			}
		} else {
			if (ladderFallSpeed > 0) entity.fallDistance = 0.0F;
			if (ladderFallSpeed > 0 && entity.motionY < -ladderFallSpeed) entity.motionY = -ladderFallSpeed;

			if (entity.isSneaking() && entity.motionY != 0.0D) entity.motionY = 0.0D;

			if (!entity.isSneaking() && entity.motionY <= 0) entity.motionY = -ladderClimbSpeed;
		}
	}

	// Modified copy of {net.minecraftforge.common.ForgeHooks.isLivingOnLadder}
	@Nullable
	public BlockPos getCollidedILadder(@Nonnull Entity entity) {
		boolean isSpectator = (entity instanceof EntityPlayer && ((EntityPlayer) entity).isSpectator());
		if (isSpectator) return null;
		AxisAlignedBB bb = entity.getEntityBoundingBox().expand(0.3, 0, 0.3);
		int mX = MathHelper.floor(bb.minX);
		int mY = MathHelper.floor(bb.minY);
		int mZ = MathHelper.floor(bb.minZ);
		for (int y2 = mY; y2 < bb.maxY; y2++) {
			for (int x2 = mX; x2 < bb.maxX; x2++) {
				for (int z2 = mZ; z2 < bb.maxZ; z2++) {
					BlockPos tmp = new BlockPos(x2, y2, z2);
					if (entity.world.getBlockState(tmp).getBlock() instanceof ILadder) return tmp;
				}
			}
		}

		return null;
	}
}
