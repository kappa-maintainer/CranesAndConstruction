package me.lordsaad.cc.api;

import me.lordsaad.cc.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
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

	public static boolean isBlockAtCraneBase(World world, BlockPos pos) {
		BlockPos down = pos.offset(EnumFacing.DOWN);
		BlockPos up = pos.offset(EnumFacing.UP);
		IBlockState stateDown = world.getBlockState(down);
		IBlockState stateUp = world.getBlockState(up);
		IBlockState state = world.getBlockState(pos);
		return (state.getBlock() == ModBlocks.CRANE_CORE
				|| state.getBlock() == ModBlocks.CRANE_BASE)
				&& (stateDown.getBlock().isBlockSolid(world, down, null)
				&& stateDown.isFullBlock())
				&& (stateUp.getBlock() == ModBlocks.CRANE_BASE
				|| stateUp.getBlock() == ModBlocks.CRANE_CORE
				|| stateUp.getBlock() == Blocks.AIR);
	}

	@Nullable
	public static BlockPos getBaseOfCrane(World world, BlockPos pos, Set<BlockPos> blocks) {
		if (!blocks.contains(pos)) blocks.add(pos);
		for (EnumFacing facing : EnumFacing.VALUES) {

			BlockPos posAdj = pos.offset(facing);
			if (blocks.contains(posAdj)) continue;

			if (isBlockAtCraneBase(world, posAdj)) return posAdj;

			getCrane(world, posAdj, blocks);
		}

		return null;
	}

	@Nullable
	public static Set<BlockPos> getCraneVerticalPole(World world, BlockPos pos, boolean checkBase, HashSet<BlockPos> blocks) {
		if (checkBase) {
			BlockPos craneBase = getBaseOfCrane(world, pos, new HashSet<>());
			if (craneBase == null) return null;
			else {
				blocks.add(craneBase);
				return getCraneVerticalPole(world, craneBase, false, blocks);
			}
		}

		if (!blocks.contains(pos)) blocks.add(pos);

		BlockPos posUp = pos.offset(EnumFacing.UP);
		IBlockState stateUp = world.getBlockState(posUp);

		if (blocks.contains(posUp)) return blocks; // RECURSION SAFETY CHECK. Should never happen unless something goes really really bad

		if (stateUp.getBlock() != ModBlocks.CRANE_CORE && stateUp.getBlock() != ModBlocks.CRANE_BASE) return blocks;

		return getCraneVerticalPole(world, posUp, false, blocks);
	}

	//public static boolean doesCraneHaveHorizontalPole(World world, BlockPos pos) {
//
	//}

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
