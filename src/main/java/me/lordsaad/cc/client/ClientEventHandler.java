package me.lordsaad.cc.client;

import me.lordsaad.cc.CCMain;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by LordSaad.
 */
public class ClientEventHandler {
	public static final ClientEventHandler INSTANCE = new ClientEventHandler();

	private ClientEventHandler() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void stitch(TextureStitchEvent.Pre event) {
		event.getMap().registerSprite(new ResourceLocation(CCMain.MOD_ID, "particles/sparkle_blurred"));
	}
}
