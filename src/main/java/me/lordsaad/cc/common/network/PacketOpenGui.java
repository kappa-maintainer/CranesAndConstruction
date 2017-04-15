package me.lordsaad.cc.common.network;

import com.teamwizardry.librarianlib.common.network.PacketBase;
import com.teamwizardry.librarianlib.common.util.saving.Save;
import me.lordsaad.cc.CCMain;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by LordSaad.
 */
public class PacketOpenGui extends PacketBase {

	@Save
	private BlockPos pos;

	public PacketOpenGui() {
	}

	public PacketOpenGui(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public void handle(MessageContext messageContext) {

	}
}
