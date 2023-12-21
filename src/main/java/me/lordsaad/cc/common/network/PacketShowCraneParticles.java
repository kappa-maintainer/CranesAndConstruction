package me.lordsaad.cc.common.network;


import com.teamwizardry.librarianlib.features.math.interpolate.StaticInterp;
import com.teamwizardry.librarianlib.features.network.PacketBase;
import com.teamwizardry.librarianlib.features.particle.ParticleBuilder;
import com.teamwizardry.librarianlib.features.particle.ParticleSpawner;
import com.teamwizardry.librarianlib.features.particle.functions.InterpFadeInOut;
import com.teamwizardry.librarianlib.features.saving.Save;
import me.lordsaad.cc.CCMain;
import me.lordsaad.cc.api.CraneManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

/**
 * Created by LordSaad.
 */
public class PacketShowCraneParticles extends PacketBase {

	@Save
	private BlockPos pos;

	public PacketShowCraneParticles() {
	}

	public PacketShowCraneParticles(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handle(MessageContext messageContext) {
		World world = Minecraft.getMinecraft().world;

		CraneManager manager = new CraneManager(world, pos);
		if (!manager.pole.isEmpty())
			for (BlockPos pos : manager.pole) {
				ParticleBuilder glitter = new ParticleBuilder(20);
				glitter.setRenderNormalLayer(new ResourceLocation(CCMain.MOD_ID, "particles/sparkle_blurred"));
				glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
				glitter.setColor(Color.GREEN);
				glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
				glitter.setScale(2);
				ParticleSpawner.spawn(glitter, world, new StaticInterp<>(new Vec3d(pos).add(0.5, 0.5, 0.5)), 1);
			}

		if (!manager.arm.isEmpty()) {
			for (BlockPos pos : manager.arm) {
				ParticleBuilder glitter = new ParticleBuilder(20);
				glitter.setRenderNormalLayer(new ResourceLocation(CCMain.MOD_ID, "particles/sparkle_blurred"));
				glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
				glitter.setColor(Color.BLUE);
				glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
				glitter.setScale(2);
				ParticleSpawner.spawn(glitter, world, new StaticInterp<>(new Vec3d(pos).add(0.5, 0.5, 0.5)), 1);
			}
		}

		if (manager.armBlock != null) {
			ParticleBuilder glitter = new ParticleBuilder(35);
			glitter.setRenderNormalLayer(new ResourceLocation(CCMain.MOD_ID, "particles/sparkle_blurred"));
			glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
			glitter.setColor(Color.BLACK);
			glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
			glitter.setScale(5);
			ParticleSpawner.spawn(glitter, world, new StaticInterp<>(new Vec3d(manager.armBlock).add(0.5, 0.5, 0.5)), 1);
		}

		if (manager.highestBlock != null) {
			ParticleBuilder glitter = new ParticleBuilder(35);
			glitter.setRenderNormalLayer(new ResourceLocation(CCMain.MOD_ID, "particles/sparkle_blurred"));
			glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
			glitter.setColor(Color.BLACK);
			glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
			glitter.setScale(5);
			ParticleSpawner.spawn(glitter, world, new StaticInterp<>(new Vec3d(manager.highestBlock).add(0.5, 0.5, 0.5)), 1);
		}

		if (manager.bottomBlock != null) {
			ParticleBuilder glitter = new ParticleBuilder(35);
			glitter.setRenderNormalLayer(new ResourceLocation(CCMain.MOD_ID, "particles/sparkle_blurred"));
			glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
			glitter.setColor(Color.BLACK);
			glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
			glitter.setScale(5);
			ParticleSpawner.spawn(glitter, world, new StaticInterp<>(new Vec3d(manager.bottomBlock).add(0.5, 0.5, 0.5)), 1);
		}
	}
}
