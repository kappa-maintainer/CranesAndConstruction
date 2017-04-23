package me.lordsaad.cc.api;

import com.teamwizardry.librarianlib.features.math.Vec2d;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

/**
 * Created by Gegy
 */
public class CraneChunkCache extends ChunkCache {
	private final BlockPos origin;
	private final int radius;

	public CraneChunkCache(World world, BlockPos from, BlockPos to, int subIn, BlockPos origin, int radius) {
		super(world, from, to, subIn);
		this.origin = origin;
		this.radius = radius;
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {
		double dist = new Vec2d(pos.getX(), pos.getZ()).add(0.5, 0.5).sub(new Vec2d(origin.getX(), origin.getZ()).add(0.5, 0.5)).length();
		if (dist <= radius)
			return super.getBlockState(pos);
		return Blocks.AIR.getDefaultState();
	}
}
