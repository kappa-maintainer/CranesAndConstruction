package me.lordsaad.cc.init;

import me.lordsaad.cc.common.block.BlockCraneSeat;
import me.lordsaad.cc.common.block.BlockScaffolding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by LordSaad.
 */
public class ModBlocks {

	public static BlockScaffolding SCAFFOLDING;
	public static BlockCraneSeat CRANE_SEAT;

	public static void init() {
		SCAFFOLDING = new BlockScaffolding();
		CRANE_SEAT = new BlockCraneSeat();
	}

	@SideOnly(Side.CLIENT)
	public static void initModel() {
		CRANE_SEAT.initModel();
	}
}
