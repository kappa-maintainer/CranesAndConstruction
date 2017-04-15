package me.lordsaad.cc.api;

import me.lordsaad.cc.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by LordSaad.
 */
public class PosUtils {

	public static BlockPos getHighestConnectedBlock(World world, BlockPos pos, Block block) {
		BlockPos up = pos.up();
		BlockPos highest = pos;
		while (!world.isAirBlock(up)) {
			if (world.getBlockState(up).getBlock() == block) highest = up;
			up = up.up();
		}

		return highest;
	}

	public static Set<BlockPos> getCrane(World world, BlockPos pos, Set<BlockPos> blocks) {
		if (!blocks.contains(pos)) blocks.add(pos);
		for (EnumFacing facing : EnumFacing.VALUES) {

			BlockPos posAdj = pos.offset(facing);
			IBlockState stateAdj = world.getBlockState(posAdj);
			if (blocks.contains(posAdj)) continue;

			if (stateAdj.getBlock() != ModBlocks.CRANE_CORE && stateAdj.getBlock() != ModBlocks.CRANE_BASE) continue;

			getCrane(world, posAdj, blocks);
		}

		return blocks;
	}

	@Nullable
	public static BlockPos findCraneSeat(World world, BlockPos blockInCrane) {
		Set<BlockPos> poses = getCrane(world, blockInCrane, new HashSet<>());
		for (BlockPos pos : poses) {
			if (world.getBlockState(pos).getBlock() == ModBlocks.CRANE_CORE) {
				return pos;
			}
		}
		return null;
	}

	public static void placeUpwardShiftedBlocks(World world, Set<BlockPos> blocks) {
		for (BlockPos pos : blocks) {
			IBlockState originalState = world.getBlockState(pos);
			world.setBlockState(pos.up(), originalState, 3);
			if (!blocks.contains(pos.down()))
				world.setBlockToAir(pos);
		}
	}
}
