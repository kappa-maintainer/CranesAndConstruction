package me.lordsaad.cc.common.network;

import com.teamwizardry.librarianlib.features.network.PacketBase;
import com.teamwizardry.librarianlib.features.saving.Save;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by LordSaad.
 */
public class PacketReduceStackFromPlayer extends PacketBase {

	@Save
	private int itemIndex;

	public PacketReduceStackFromPlayer() {
	}

	public PacketReduceStackFromPlayer(int itemIndex) {
		this.itemIndex = itemIndex;
	}

	@Override
	public void handle(MessageContext messageContext) {
		EntityPlayer player = messageContext.getServerHandler().playerEntity;
		ItemStack stack = player.inventory.getStackInSlot(itemIndex);
		stack.setCount(stack.getCount() - 1);
	}
}
