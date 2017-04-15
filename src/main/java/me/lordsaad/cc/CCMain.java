package me.lordsaad.cc;

import com.teamwizardry.librarianlib.LibrarianLog;
import com.teamwizardry.librarianlib.common.base.ModCreativeTab;
import me.lordsaad.cc.common.CommonProxy;
import me.lordsaad.cc.init.ModBlocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import javax.annotation.Nonnull;

@Mod(
		modid = CCMain.MOD_ID,
		name = CCMain.MOD_NAME,
		version = CCMain.VERSION
)
public class CCMain {

	public static final String MOD_ID = "cc";
	public static final String MOD_NAME = "Cranes & Construction";
	public static final String VERSION = "1.0";

	public static final String CLIENT = "me.lordsaad.cc.client.ClientProxy";
	public static final String SERVER = "me.lordsaad.cc.common.CommonProxy";

	@SidedProxy(clientSide = CLIENT, serverSide = SERVER)
	public static CommonProxy proxy;

	@Mod.Instance
	public static CCMain instance;

	public static ModCreativeTab tab = new ModCreativeTab(MOD_NAME) {
		@Override
		@Nonnull
		public ItemStack getTabIconItem() {
			return new ItemStack(ModBlocks.CRANE_BASE);
		}

		@Override
		@Nonnull
		public ItemStack getIconStack() {
			return new ItemStack(ModBlocks.CRANE_BASE);
		}
	};

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
