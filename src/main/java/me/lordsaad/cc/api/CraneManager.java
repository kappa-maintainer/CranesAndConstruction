package me.lordsaad.cc.api;

import kotlin.Pair;
import me.lordsaad.cc.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;

/**
 * Created by LordSaad.
 */
public class CraneManager {

	public boolean didSomethingGoBad = false;

	public int width = 0, height;

	@Nonnull
	public World world;

	@Nonnull
	public HashSet<BlockPos> allCraneBlocks = new HashSet<>(100), pole = new HashSet<>(50), arm = new HashSet<>(50);

	@Nullable
	public BlockPos bottomBlock, highestBlock, armBlock, seat, blockInCrane;

	@Nullable
	public EnumFacing direction;

	public CraneManager(@Nonnull World world, @Nonnull BlockPos blockInCrane) {
		this.world = world;
		this.blockInCrane = blockInCrane;

		refresh(blockInCrane);
	}

	public static boolean hasPole(@Nonnull World world, @Nonnull BlockPos pos) {
		if (isBlockAtCraneBottom(world, pos)) return true;
		else {
			BlockPos shift = pos;
			IBlockState shiftState = world.getBlockState(shift);
			int count = 0;
			while ((shiftState.getBlock() == ModBlocks.CRANE_SEAT || shiftState.getBlock() == ModBlocks.SCAFFOLDING)) {
				if (isBlockAtCraneBottom(world, shift)) return true;
				count++;
				shift = shift.down();
				shiftState = world.getBlockState(shift);
				if (count >= 50) break;
			}
			return false;
		}
	}

	public static boolean isBlockAtCraneBottom(@Nonnull World world, @Nonnull BlockPos pos) {
		if (!world.isBlockLoaded(pos)) return false;

		BlockPos down = pos.offset(EnumFacing.DOWN);
		BlockPos up = pos.offset(EnumFacing.UP);

		IBlockState stateDown = world.getBlockState(down);
		IBlockState stateUp = world.getBlockState(up);
		IBlockState state = world.getBlockState(pos);

		return (state.getBlock() == ModBlocks.CRANE_SEAT
				|| state.getBlock() == ModBlocks.SCAFFOLDING)
				&& stateDown.isFullBlock()
				&& (stateUp.getBlock() == ModBlocks.SCAFFOLDING
				|| stateUp.getBlock() == ModBlocks.CRANE_SEAT
				|| stateUp.getBlock() == Blocks.AIR);
	}

	public void refresh(BlockPos blockInCrane) {
		// Can never be too safe.
		if (!world.isBlockLoaded(blockInCrane)) {
			didSomethingGoBad = true;
			return;
		}

		// Get all possible connected crane blocks.
		allCraneBlocks.clear();
		getAllCrane(blockInCrane);
		if (allCraneBlocks.isEmpty()) {
			didSomethingGoBad = true;
			return;
		}

		// Get the base or bottom of the crane. The lowest point possible.
		bottomBlock = null;
		BlockPos bottomBlock = getBaseOfCrane();
		if (bottomBlock == null) {
			didSomethingGoBad = true;
			return;
		} else this.bottomBlock = bottomBlock;

		// Get the highest point of the crane from the bottom block.
		highestBlock = null;
		BlockPos highestBlock = getHighestCranePoint();
		if (highestBlock == null) {
			didSomethingGoBad = true;
			return;
		} else this.highestBlock = highestBlock;

		// Get all the blocks between the highest and lowest points on the crane.
		pole.clear();
		getCranePole();
		if (pole.isEmpty()) {
			didSomethingGoBad = true;
			return;
		} else height = pole.size();

		// Get the seat of the crane
		seat = null;
		BlockPos seat = getCraneSeat();
		if (seat == null) {
			didSomethingGoBad = true;
		} else this.seat = seat;

		// Get the position of the arm on the pole and the direction the crane is facing.
		armBlock = null;
		direction = null;
		Pair<BlockPos, EnumFacing> pair = getCraneArmPosAndDirection();
		if (pair == null) {
			didSomethingGoBad = true;
			return;
		} else {
			armBlock = pair.getFirst();
			direction = pair.getSecond();
		}

		if (armBlock == null || direction == null) {
			didSomethingGoBad = true;
			return;
		}

		// Get the arm blocks of the crane from the arm block pos and direction.
		arm.clear();
		getCraneArm();
		if (arm.isEmpty()) {
			didSomethingGoBad = true;
		} else width = arm.size();

		allCraneBlocks = new HashSet<>();
		allCraneBlocks.addAll(pole);
		allCraneBlocks.addAll(arm);
	}

	public void shrinkCrane(@Nonnull BlockPos anchor) {
		if (allCraneBlocks.isEmpty()) return;

		HashSet<Pair<IBlockState, BlockPos>> structure = new HashSet<>(100);

		for (BlockPos pos : allCraneBlocks) {
			if (!world.isBlockLoaded(pos)) continue;
			if (pos.getY() <= anchor.getY()) continue;
			IBlockState oldState = world.getBlockState(pos);
			structure.add(new Pair<>(oldState, pos));

			world.setBlockToAir(pos);
		}

		for (Pair<IBlockState, BlockPos> pair : structure) {
			if (pair.getSecond().getY() <= anchor.getY()) continue;
			world.setBlockState(pair.getSecond().down(), pair.getFirst(), 3);
		}

		refresh(blockInCrane);
	}

	public void elongateCrane(BlockPos anchor) {
		if (allCraneBlocks.isEmpty()) return;
		if (arm.contains(anchor)) return;

		HashSet<Pair<IBlockState, BlockPos>> structure = new HashSet<>(100);

		for (BlockPos pos : allCraneBlocks) {
			if (!world.isBlockLoaded(pos)) continue;
			if (pos.getY() <= anchor.getY()) continue;
			IBlockState oldState = world.getBlockState(pos);
			structure.add(new Pair<>(oldState, pos));

			world.setBlockToAir(pos);
		}

		for (Pair<IBlockState, BlockPos> pair : structure)
			world.setBlockState(pair.getSecond().up(), pair.getFirst(), 3);

		world.setBlockState(anchor.up(), ModBlocks.SCAFFOLDING.getDefaultState(), 3);

		refresh(blockInCrane);
	}

	private void getAllCrane(@Nonnull BlockPos pos) {
		if (allCraneBlocks.size() >= 100) return;

		if (!allCraneBlocks.contains(pos))
			if (world.isBlockLoaded(pos))
				allCraneBlocks.add(pos);

		for (EnumFacing facing : EnumFacing.VALUES) {
			BlockPos posAdj = pos.offset(facing);
			IBlockState stateAdj = world.getBlockState(posAdj);

			if (!world.isBlockLoaded(posAdj)) continue;
			if (allCraneBlocks.contains(posAdj)) continue;
			if (stateAdj.getBlock() != ModBlocks.CRANE_SEAT && stateAdj.getBlock() != ModBlocks.SCAFFOLDING) continue;

			getAllCrane(posAdj);
		}
	}

	@Nullable
	private BlockPos getHighestCranePoint() {
		if (bottomBlock == null) return null;
		if (!world.isBlockLoaded(bottomBlock)) return null;

		IBlockState state = world.getBlockState(bottomBlock);
		BlockPos offset = bottomBlock;
		int count = 0;
		while (count < 50 && (state.getBlock() == ModBlocks.CRANE_SEAT || state.getBlock() == ModBlocks.SCAFFOLDING)) {
			count++;
			BlockPos pos = bottomBlock.offset(EnumFacing.UP, count);
			IBlockState shiftedState = world.getBlockState(pos);
			if (shiftedState.getBlock() != ModBlocks.CRANE_SEAT && shiftedState.getBlock() != ModBlocks.SCAFFOLDING)
				break;

			offset = pos;
			state = shiftedState;
		}

		return offset;
	}

	private void getCranePole() {
		if (bottomBlock == null || highestBlock == null) return;
		if (!world.isBlockLoaded(bottomBlock)) return;
		if (bottomBlock.getX() != highestBlock.getX() || bottomBlock.getZ() != highestBlock.getZ()) return;

		for (int i = bottomBlock.getY(); i < highestBlock.getY() + 1; i++) {
			BlockPos pos = new BlockPos(bottomBlock.getX(), i, bottomBlock.getZ());
			if (!pole.contains(pos)) pole.add(pos);
		}
	}

	@Nullable
	private Pair<BlockPos, EnumFacing> getCraneArmPosAndDirection() {
		if (pole.isEmpty()) return null;

		for (BlockPos pos : pole)
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				BlockPos posAdj = pos.offset(facing);
				if (!world.isBlockLoaded(posAdj)) continue;

				IBlockState stateAdj = world.getBlockState(posAdj);
				if (stateAdj.getBlock() != ModBlocks.SCAFFOLDING) continue;

				return new Pair<>(pos, facing);
			}

		return null;
	}

	private void getCraneArm() {
		if (armBlock == null || direction == null) return;

		IBlockState state = world.getBlockState(armBlock);
		BlockPos offset = armBlock;
		int count = 0;
		while (count < 50 && (state.getBlock() == ModBlocks.CRANE_SEAT || state.getBlock() == ModBlocks.SCAFFOLDING) && world.isBlockLoaded(offset)) {
			count++;
			arm.add(offset);
			offset = armBlock.offset(direction, count);
			state = world.getBlockState(offset);
		}
	}

	@Nullable
	private BlockPos getCraneSeat() {
		if (pole.isEmpty()) return null;

		Deque<BlockPos> seats = new ArrayDeque<>();
		for (BlockPos pos : pole) {
			if (!world.isBlockLoaded(pos)) continue;
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock() == ModBlocks.CRANE_SEAT) seats.add(pos);
		}

		if (seats.isEmpty() || seats.size() <= 1 || seats.size() > 2) return null;

		BlockPos seat1 = seats.pop();
		BlockPos seat2 = seats.pop();
		if (seat1.getY() + 1 == seat2.getY() || seat1.getY() - 1 == seat2.getY())
			return seat1.getY() < seat2.getY() ? seat1 : seat2;

		return null;
	}

	@Nullable
	private BlockPos getBaseOfCrane() {
		if (allCraneBlocks.isEmpty()) return null;

		for (BlockPos block : allCraneBlocks)
			if (isBlockAtCraneBottom(world, block)) {
				return block;
			}

		return null;
	}
}
