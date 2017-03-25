package me.lordsaad.cc;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(
		modid = CCMain.MOD_ID,
		name = CCMain.MOD_NAME,
		version = CCMain.VERSION
)
public class CCMain {

	public static final String MOD_ID = "cc";
	public static final String MOD_NAME = "CCMain";
	public static final String VERSION = "1.0";

	@EventHandler
	public void init(FMLInitializationEvent event) {

	}
}
