package me.lordsaad.cc.api;

import kotlin.Pair;
import me.lordsaad.cc.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by LordSaad.
 */
public class PosUtils {

	public static Vec3d getVectorForRotation(float pitch, float yaw) {
		float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
		float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
		float f2 = -MathHelper.cos(-pitch * 0.017453292F);
		float f3 = MathHelper.sin(-pitch * 0.017453292F);
		return new Vec3d((double) (f1 * f2), (double) f3, (double) (f * f2));
	}


	public static Vec3d vecFromRotations(float rotationPitch, float rotationYaw) {
		return Vec3d.fromPitchYaw(rotationPitch, rotationYaw);
	}

	public static float[] vecToRotations(Vec3d vec) {
		float yaw = (float) MathHelper.atan2(vec.zCoord, vec.xCoord);
		float pitch = (float) Math.asin(vec.yCoord / vec.lengthVector());
		return new float[]{(float) Math.toDegrees(pitch), (float) Math.toDegrees(yaw) + 90};
	}

	@Nullable
	public static BlockPos getHighestCranePoint(HashSet<BlockPos> blocks) {
		if (blocks == null) return null;
		BlockPos highest = BlockPos.ORIGIN;
		for (BlockPos block : blocks) {
			if (block.getY() > highest.getY()) highest = block;
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
	public static BlockPos getBaseOfCrane(World world, BlockPos pos, HashSet<BlockPos> blocks) {
		getCrane(world, pos, blocks);

		for (BlockPos block : blocks)
			if (isBlockAtCraneBase(world, block))
				return block;

		return null;
	}

	@Nullable
	public static HashSet<BlockPos> getCraneVerticalPole(World world, BlockPos pos, boolean checkBase, HashSet<BlockPos> blocks) {
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

		if (blocks.contains(posUp))
			return blocks; // RECURSION SAFETY CHECK. Should never happen unless something goes really really bad

		if (stateUp.getBlock() != ModBlocks.CRANE_CORE && stateUp.getBlock() != ModBlocks.CRANE_BASE) return blocks;

		return getCraneVerticalPole(world, posUp, false, blocks);
	}

	@Nullable
	public static Pair<BlockPos, EnumFacing> getHorizontalOriginAndDirection(World world, BlockPos pos) {
		HashSet<BlockPos> blocks = getCraneVerticalPole(world, pos, true, new HashSet<>());
		if (blocks == null) return null;

		BlockPos horizontalCenter = null;
		for (BlockPos polePos : blocks) {
			for (EnumFacing side : EnumFacing.HORIZONTALS) {
				IBlockState state = world.getBlockState(polePos.offset(side));
				if (state.getBlock() == ModBlocks.CRANE_BASE || state.getBlock() == ModBlocks.CRANE_CORE) {
					horizontalCenter = polePos;
					break;
				}
			}
		}

		if (horizontalCenter == null) return null;

		Pair<BlockPos, EnumFacing> pair = null;
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			IBlockState state = world.getBlockState(horizontalCenter.offset(facing));
			if (state.getBlock() == ModBlocks.CRANE_BASE || state.getBlock() == ModBlocks.CRANE_CORE) {
				if (pair == null)
					pair = new Pair<>(horizontalCenter, facing);
				else return null;
			}
		}
		return pair;
	}

	@Nullable
	public static HashSet<BlockPos> getCraneHorizontalPole(World world, BlockPos pos, EnumFacing direction, HashSet<BlockPos> blocks) {
		if (!blocks.contains(pos)) blocks.add(pos);

		BlockPos frontPos = pos.offset(direction);
		if (blocks.contains(frontPos)) return blocks;

		IBlockState frontState = world.getBlockState(frontPos);
		if (frontState.getBlock() == ModBlocks.CRANE_BASE || frontState.getBlock() == ModBlocks.CRANE_CORE) {
			return getCraneHorizontalPole(world, frontPos, direction, blocks);
		} else return blocks;
	}

	@Nullable
	public static HashSet<BlockPos> getCraneHorizontalPole(World world, BlockPos pos) {
		Pair<BlockPos, EnumFacing> horizontalPoleOriginPair = getHorizontalOriginAndDirection(world, pos);
		if (horizontalPoleOriginPair == null) return null;

		return getCraneHorizontalPole(world, horizontalPoleOriginPair.getFirst(), horizontalPoleOriginPair.getSecond(), new HashSet<>());
	}

	public static HashSet<BlockPos> getCrane(World world, BlockPos pos, HashSet<BlockPos> blocks) {
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
}
