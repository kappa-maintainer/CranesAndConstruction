package me.lordsaad.cc;

import com.teamwizardry.librarianlib.core.LibrarianLog;
import me.lordsaad.cc.common.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
		modid = CCMain.MOD_ID,
		name = CCMain.MOD_NAME,
		version = CCMain.VERSION,
		dependencies = "required-after:librarianlib"
)
public class CCMain {

	public static final String MOD_ID = "cc";
	public static final String MOD_NAME = "Cranes & Construction";
	public static final String VERSION = "1.4";

	public static final String CLIENT = "me.lordsaad.cc.client.ClientProxy";
	public static final String SERVER = "me.lordsaad.cc.common.CommonProxy";

	@SidedProxy(clientSide = CLIENT, serverSide = SERVER)
	public static CommonProxy proxy;

	@Mod.Instance
	public static CCMain instance;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LibrarianLog.INSTANCE.info("Building up the scaffolding!");

		proxy.preInit(event);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent e) {
		proxy.init(e);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		proxy.postInit(e);
	}
}
