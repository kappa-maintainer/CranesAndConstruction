package me.lordsaad.cc.client.render;

import me.lordsaad.cc.CCMain;
import me.lordsaad.cc.common.tile.TileCraneCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;

/**
 * Created by LordSaad.
 */
public class RenderCraneCore extends TileEntitySpecialRenderer<TileCraneCore> {

	private IBakedModel modelCraneBase = null;

	@Override
	public void renderTileEntityAt(TileCraneCore te, double x, double y, double z, float partialTicks, int destroyStage) {
		super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage);

		if (modelCraneBase == null) {
			IModel model = null;
			try {
				model = ModelLoaderRegistry.getModel(new ResourceLocation(CCMain.MOD_ID, "block/crane_base"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			modelCraneBase = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
					location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
		}

		double subtractedMillis = (te.getWorld().getTotalWorldTime() - te.worldTime);
		double transitionTimeMax = Math.max(10, Math.min(Math.abs((te.prevYaw - te.destYaw) / 2.0), 35));
		float yaw = te.currentYaw;

		if (true) {
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

			if (te.originalDirection == null) return;

			GlStateManager.translate(0.5, 0, 0.5);
			for (int i = 0; i < te.armLength; i++) {
				BlockPos posOffset = BlockPos.ORIGIN.offset(te.originalDirection, i);
				GlStateManager.translate(posOffset.getX(), 0, posOffset.getZ());
				Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(modelCraneBase, 1.0F, 1, 1, 1);
				GlStateManager.translate(-posOffset.getX(), 0, -posOffset.getZ());
			}
			GlStateManager.popMatrix();
		}
	}
}
