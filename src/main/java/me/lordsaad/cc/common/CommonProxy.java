package me.lordsaad.cc.common;

import com.teamwizardry.librarianlib.common.network.PacketHandler;
import me.lordsaad.cc.common.network.PacketSendBlockToCrane;
import me.lordsaad.cc.init.ModBlocks;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by LordSaad.
 */
public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {
		ModBlocks.init();

		PacketHandler.register(PacketSendBlockToCrane.class, Side.SERVER);
	}

	public void init(FMLInitializationEvent event) {

	}

	public void postInit(FMLPostInitializationEvent event) {

	}
}
