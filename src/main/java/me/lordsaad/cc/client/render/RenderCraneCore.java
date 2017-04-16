package me.lordsaad.cc.client.render;

import me.lordsaad.cc.common.tile.TileCraneCore;
import me.lordsaad.cc.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

/**
 * Created by LordSaad.
 */
public class RenderCraneCore extends TileEntitySpecialRenderer<TileCraneCore> {

	@Override
	public void renderTileEntityAt(TileCraneCore te, double x, double y, double z, float partialTicks, int destroyStage) {
		super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage);

		double subtractedMillis = (te.getWorld().getTotalWorldTime() - te.worldTime);
		double transitionTimeMax = Math.max(10, Math.min(Math.abs((te.prevYaw - te.destYaw) / 2.0), 35));
		float yaw = te.currentYaw;

		if (te.transitionArm && te.nextPair != null) {
			GlStateManager.pushMatrix();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.enableLighting();

			GlStateManager.translate(x, y, z);

			if (subtractedMillis < transitionTimeMax) {
				if (Math.round(te.destYaw) > Math.round(te.prevYaw))
					yaw = -((te.destYaw - te.prevYaw) / 2) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.destYaw + te.prevYaw) / 2;
				else
					yaw = ((te.prevYaw - te.destYaw) / 2) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.destYaw + te.prevYaw) / 2;
			} else yaw = te.destYaw;

			GlStateManager.rotate(yaw, 0, 1, 0);

			EnumFacing facing = EnumFacing.fromAngle(te.prevYaw);

			for (int i = 1; i < 20; i++) {
				BlockPos posOffset = BlockPos.ORIGIN.offset(facing, i);
				IBlockState craneBase = ModBlocks.CRANE_BASE.getDefaultState();

				Tessellator tes = Tessellator.getInstance();
				VertexBuffer buffer = tes.getBuffer();
				BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

				Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

				dispatcher.getBlockModelRenderer().renderModelFlat(Minecraft.getMinecraft().world, dispatcher.getModelForState(craneBase), craneBase, posOffset, buffer, false, 0);

				tes.draw();
			}

			GlStateManager.popMatrix();
		}
	}
}
