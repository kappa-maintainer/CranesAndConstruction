package me.lordsaad.cc.init;

import com.teamwizardry.librarianlib.features.base.ModCreativeTab;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Created by LordSaad.
 */
public class ModTab extends ModCreativeTab {

	public ModTab() {
		super();
		registerDefaultTab();
	}

	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(ModBlocks.CRANE_SEAT);
	}

	@Nonnull
	@Override
	public ItemStack getIconStack() {
		return new ItemStack(ModBlocks.CRANE_SEAT);
	}
}
