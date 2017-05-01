package me.lordsaad.cc.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashSet;

/**
 * Created by LordSaad.
 */
public class PosUtils {

	public static boolean isPosInSet(@Nonnull BlockPos pos, HashSet<BlockPos> blocks) {
		for (BlockPos block : blocks)
			if (block.toLong() == pos.toLong()) return true;

		return false;
	}

	public static BlockPos getHighestBlock(World world, BlockPos pos) {
		BlockPos.MutableBlockPos highest = new BlockPos.MutableBlockPos(pos.getX(), 255, pos.getZ());
		IBlockState stateHighest = world.getBlockState(highest);
		while (world.isAirBlock(highest) || stateHighest.getMaterial().isLiquid()) {
			if (highest.getY() <= 0) {
				break;
			}
			highest.move(EnumFacing.DOWN);
			stateHighest = world.getBlockState(highest);
		}

		return highest;
	}
}
