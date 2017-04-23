package me.lordsaad.cc.api;

import net.minecraft.util.math.BlockPos;

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
}
