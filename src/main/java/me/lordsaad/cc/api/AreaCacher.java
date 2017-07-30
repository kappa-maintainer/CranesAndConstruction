package me.lordsaad.cc.api;

import com.google.common.collect.HashMultimap;
import com.teamwizardry.librarianlib.features.math.Vec2d;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.EnumMap;
import java.util.HashSet;

/**
 * Created by LordSaad.
 */
public class AreaCacher {

	public EnumMap<BlockRenderLayer, HashMultimap<IBlockState, BlockPos>> blocks = new EnumMap<>(BlockRenderLayer.class);

	private HashSet<BlockPos> tempPosCache = new HashSet<>();

	/**
	 * Will cache the area from the crane selected.
	 *
	 * @param world  The world object.
	 * @param origin A block in the crane.
	 * @param width  The width of the crane.
	 * @param height The height of the crane.
	 */
	public AreaCacher(World world, BlockPos origin, int width, int height) {
		IBlockState[][][] tempStateCache = new IBlockState[width * 2][height * 2][width * 2];

		// FIRST ITERATION
		// Save everything. Check surroundings in second iteration.
		for (int i = -width; i < width; i++) {
			for (int j = -width; j < width; j++) {
				for (int k = -height; k < height; k++) {
					BlockPos pos = new BlockPos(origin.getX() + i, origin.getY() + k, origin.getZ() + j);

					double dist = new Vec2d(pos.getX(), pos.getZ()).sub(new Vec2d(origin.getX(), origin.getZ())).length();
					if (dist > width || world.isAirBlock(pos)) continue;

					IBlockState state = world.getBlockState(pos);

					BlockPos sub = pos.subtract(origin).add(width, height, width);
					tempStateCache[sub.getX()][sub.getY()][sub.getZ()] = state;
				}
			}
		}

		// SECOND ITERATION
		// Check surrounding iterations
		for (int i = 0; i < tempStateCache.length; i++) {
			for (int j = 0; j < tempStateCache[0].length; j++) {
				for (int k = 0; k < tempStateCache.length; k++) {
					IBlockState state = tempStateCache[i][j][k];
					if (state == null) continue;

					boolean surrounded = true;
					for (EnumFacing facing : EnumFacing.VALUES) {
						BlockPos offset = new BlockPos(i, j, k).offset(facing);
						if (offset.getX() < 0
								|| offset.getY() < 0
								|| offset.getZ() < 0
								|| offset.getX() >= tempStateCache.length
								|| offset.getY() >= tempStateCache[0].length
								|| offset.getZ() >= tempStateCache.length) {
							continue;
						}

						double dist = new Vec2d(offset.getX() - width, offset.getZ() - width).length();
						if (dist > width) continue;

						IBlockState offsetState = tempStateCache[offset.getX()][offset.getY()][offset.getZ()];
						if (offsetState == null) {
							surrounded = false;
							break;
						}

						if (!offsetState.isFullBlock()
								|| !offsetState.isOpaqueCube()
								|| !offsetState.isBlockNormalCube()
								|| !offsetState.isNormalCube()
								|| offsetState.isTranslucent()
								|| offsetState.getMaterial().isLiquid()
								|| !offsetState.getMaterial().isSolid()) {
							surrounded = false;
							break;
						}
					}

					if (!surrounded) {
						BlockPos pos = new BlockPos(i, j, k).subtract(new Vec3i(width, height, width)).add(origin);
						BlockRenderLayer layer = state.getBlock().getBlockLayer();
						HashMultimap<IBlockState, BlockPos> multimap = blocks.get(layer);
						if (multimap == null) multimap = HashMultimap.create();
						multimap.put(state, pos);
						blocks.put(layer, multimap);
					}
				}
			}
		}
	}
}
