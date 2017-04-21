package me.lordsaad.cc.common.network;


import com.teamwizardry.librarianlib.features.math.interpolate.StaticInterp;
import com.teamwizardry.librarianlib.features.network.PacketBase;
import com.teamwizardry.librarianlib.features.particle.ParticleBuilder;
import com.teamwizardry.librarianlib.features.particle.ParticleSpawner;
import com.teamwizardry.librarianlib.features.particle.functions.InterpFadeInOut;
import com.teamwizardry.librarianlib.features.saving.Save;
import me.lordsaad.cc.CCMain;
import me.lordsaad.cc.api.PosUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.HashSet;

/**
 * Created by LordSaad.
 */
public class PacketShowCraneParticles extends PacketBase {

	@Save
	private BlockPos pos;
	@Save
	private Color color;

	public PacketShowCraneParticles() {
	}

	public PacketShowCraneParticles(BlockPos pos, Color color) {

		this.pos = pos;
		this.color = color;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handle(MessageContext messageContext) {
		World world = Minecraft.getMinecraft().world;

		HashSet<BlockPos> blocks = PosUtils.getCrane(world, pos, new HashSet<>());
		if (blocks != null && !blocks.isEmpty())
			for (BlockPos pos : blocks) {
				ParticleBuilder glitter = new ParticleBuilder(10);
				glitter.setRenderNormalLayer(new ResourceLocation(CCMain.MOD_ID, "particles/sparkle_blurred"));
				glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
				glitter.setColor(color);
				glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
				glitter.setScale(2);
				ParticleSpawner.spawn(glitter, world, new StaticInterp<>(new Vec3d(pos).addVector(0.5, 0.5, 0.5)), 1);
			}
	}
}
