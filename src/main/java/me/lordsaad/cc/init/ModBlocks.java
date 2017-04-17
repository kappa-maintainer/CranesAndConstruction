package me.lordsaad.cc.init;

import me.lordsaad.cc.common.block.BlockCraneBase;
import me.lordsaad.cc.common.block.BlockCraneCore;
import me.lordsaad.cc.common.block.BlockCraneHandle;

/**
 * Created by LordSaad.
 */
public class ModBlocks {

	public static BlockCraneBase CRANE_BASE;
	public static BlockCraneCore CRANE_CORE;
	public static BlockCraneHandle CRANE_HANDLE;

	public static void init() {
		CRANE_BASE = new BlockCraneBase();
		CRANE_CORE = new BlockCraneCore();
		CRANE_HANDLE = new BlockCraneHandle();
	}

	public static void initModel() {
		CRANE_CORE.initModel();
	}
}
