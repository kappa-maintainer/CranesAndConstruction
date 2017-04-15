package me.lordsaad.cc.api;

import me.lordsaad.cc.common.entity.EntitySeat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class SittingUtil {

	public static boolean seatPlayer(World world, BlockPos position, EntityPlayer player) {
		if (!checkForSeats(world, position)) {
			EntitySeat seat = new EntitySeat(world);
			seat.setPositionAndUpdate(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
			world.spawnEntity(seat);
			boolean b = player.startRiding(seat);
			if (!b) {
				seat.setDead();
				return false;
			} else return true;
		}
		return false;
	}

	public static boolean checkForSeats(World world, BlockPos pos) {
		List<EntitySeat> seatList = world.getEntitiesWithinAABB(EntitySeat.class, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 1.0D, pos.getZ() + 1.0D).expand(1D, 1D, 1D));
		seatList.removeIf(seat -> {
			for (Entity entity : seat.getPassengers()) {
				if (entity.isSneaking()) {
					world.removeEntity(seat);
					return true;
				}
			}
			if (seat.getPassengers().isEmpty() || world.isAirBlock(seat.getPosition())) {
				world.removeEntity(seat);
				return true;
			}
			return false;
		});
		return !seatList.isEmpty();
	}
}
