package me.lordsaad.cc.client;

import me.lordsaad.cc.CCMain;
import me.lordsaad.cc.client.gui.GuiHandler;
import me.lordsaad.cc.common.CommonProxy;
import me.lordsaad.cc.init.ModBlocks;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * Created by LordSaad.
 */
public class ClientProxy extends CommonProxy implements IResourceManagerReloadListener {

	public static BlockFluidRenderer fluidRenderer;

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);

		ModBlocks.initModel();
		NetworkRegistry.INSTANCE.registerGuiHandler(CCMain.instance, new GuiHandler());
		ClientEventHandler.INSTANCE.getClass();

	}

	@Override
	public void init(FMLInitializationEvent event) {

	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
		fluidRenderer = new BlockFluidRenderer(new BlockColors());
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		fluidRenderer = new BlockFluidRenderer(new BlockColors());
	}
}
