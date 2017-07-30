package me.lordsaad.cc.client.render;

import me.lordsaad.cc.CCMain;
import me.lordsaad.cc.common.tile.TileCraneCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
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

	private IBakedModel modelCraneBase = null, modelCraneHandle;

	@Override
	public void render(TileCraneCore te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		super.render(te, x, y, z, partialTicks, destroyStage, alpha);

		IModel model = null;
		if (modelCraneBase == null) {
			try {
				model = ModelLoaderRegistry.getModel(new ResourceLocation(CCMain.MOD_ID, "block/scaffolding"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			modelCraneBase = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
					location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
		}
		//if (modelCraneHandle == null) {
		//	try {
		//		model = ModelLoaderRegistry.getModel(new ResourceLocation(CCMain.MOD_ID, "block/crane_handle"));
		//	} catch (Exception e) {
		//		e.printStackTrace();
		//	}
		//	modelCraneHandle = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
		//			location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
		//}

		double subtractedMillis = (te.getWorld().getTotalWorldTime() - te.worldTime);
		double transitionTimeMax = Math.max(10, Math.min(Math.abs((te.prevYaw - te.destYaw) / 2.0), 20));
		float yaw;

		if (subtractedMillis < transitionTimeMax) {
			if (Math.round(te.destYaw) > Math.round(te.prevYaw))
				yaw = -((te.destYaw - te.prevYaw) / 2) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.destYaw + te.prevYaw) / 2;
			else
				yaw = ((te.prevYaw - te.destYaw) / 2) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.destYaw + te.prevYaw) / 2;
		} else yaw = te.destYaw;

		if (te.originalDirection == null) return;

		if (te.transitionArmToOrigin || te.transitionArm || !te.queue.isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.enableLighting();
			GlStateManager.enableRescaleNormal();
			GlStateManager.color(1, 1, 1);

			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

			GlStateManager.translate(x + 0.5, y + te.originalArmPos.getY() - te.getPos().getY(), z + 0.5);
			GlStateManager.rotate(yaw, 0, 1, 0);
			GlStateManager.translate(-0.5, 0, -0.5);
			bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

			for (int i = 1; i < te.armLength; i++) {
				BlockPos posOffset = BlockPos.ORIGIN.offset(te.originalDirection, i);
				GlStateManager.translate(posOffset.getX(), 0, posOffset.getZ());
				Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(modelCraneBase, 1.0F, 1, 1, 1);
				GlStateManager.translate(-posOffset.getX(), 0, -posOffset.getZ());
			}
			GlStateManager.popMatrix();

			// TODO
			/*
			///////////////////////////
			//        HANDLE         //
			///////////////////////////
			GlStateManager.pushMatrix();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.enableLighting();

			double currentX, currentY;
			if (subtractedMillis < transitionTimeMax) {
				if (Math.round(te.handleTo.getX()) > Math.round(te.handleFrom.getX()))
					currentX = -((te.handleTo.getX() - te.handleFrom.getX()) / 2.0) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.handleTo.getX() + te.handleFrom.getX()) / 2.0;
				else
					currentX = ((te.handleFrom.getX() - te.handleTo.getX()) / 2.0) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.handleTo.getX() + te.handleFrom.getX()) / 2.0;
			} else currentX = te.handleTo.getX();

			if (subtractedMillis < transitionTimeMax) {
				if (Math.round(te.handleTo.getY()) > Math.round(te.handleFrom.getY()))
					currentY = -((te.handleTo.getY() - te.handleFrom.getY()) / 2.0) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.handleTo.getY() + te.handleFrom.getY()) / 2.0;
				else
					currentY = ((te.handleFrom.getY() - te.handleTo.getY()) / 2.0) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.handleTo.getY() + te.handleFrom.getY()) / 2.0;
			} else currentY = te.handleTo.getY();

			GlStateManager.translate(x + 0.5, y + te.originalArmPos.getY() - te.getPos().getY(), z + 0.5);
			GlStateManager.rotate(yaw, 0, 1, 0);
			GlStateManager.translate(-0.5, 0, -0.5);
			BlockPos posOffset = BlockPos.ORIGIN.offset(te.originalDirection);
			GlStateManager.translate(posOffset.getX(), 0, posOffset.getZ());

			BlockPos blockpos = te.getPos().subtract(new BlockPos(currentX, te.originalArmPos.getY() - 1, currentY));
			GlStateManager.rotate(yaw, 0, 1, 0);
			//GlStateManager.translate((float) blockpos.getX() - 0.5, (float) blockpos.getY(), (float) blockpos.getZ() - 0.5);
			//GlStateManager.translate(currentX, -1, currentY);
			Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(modelCraneHandle, 1.0F, 1, 1, 1);

			GlStateManager.popMatrix();

		}

		///////////////////////////
		//         SAND          //
		///////////////////////////

		if (te.transitionArm) {

			if (te.nextPair != null) {
				GlStateManager.pushMatrix();
				GlStateManager.disableCull();

				BlockPos initialOffset = te.getPos().offset(te.originalDirection.getOpposite());
				BlockPos adjustedY = new BlockPos(initialOffset.getX(), te.originalArmPos.getY() - 1, initialOffset.getZ());
				BlockPos relative = te.getPos().subtract(adjustedY);
				GlStateManager.translate(relative.getX(), 0, relative.getZ());

				double currentX, currentY;
				if (subtractedMillis < transitionTimeMax) {
					if (Math.round(te.handleTo.getX()) > Math.round(te.handleFrom.getX()))
						currentX = -((te.handleTo.getX() - te.handleFrom.getX()) / 2.0) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.handleTo.getX() + te.handleFrom.getX()) / 2.0;
					else
						currentX = ((te.handleFrom.getX() - te.handleTo.getX()) / 2.0) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.handleTo.getX() + te.handleFrom.getX()) / 2.0;
				} else currentX = te.handleTo.getX();

				if (subtractedMillis < transitionTimeMax) {
					if (Math.round(te.handleTo.getY()) > Math.round(te.handleFrom.getY()))
						currentY = -((te.handleTo.getY() - te.handleFrom.getY()) / 2.0) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.handleTo.getY() + te.handleFrom.getY()) / 2.0;
					else
						currentY = ((te.handleFrom.getY() - te.handleTo.getY()) / 2.0) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.handleTo.getY() + te.handleFrom.getY()) / 2.0;
				} else currentY = te.handleTo.getY();

				GlStateManager.disableLighting();
				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer vertexbuffer = tessellator.getBuffer();

				vertexbuffer.begin(7, DefaultVertexFormats.BLOCK);
				BlockPos blockpos = new BlockPos(currentX, te.originalArmPos.getY() - 1, currentY);

				GlStateManager.translate(x, y, z);
				GlStateManager.rotate(yaw, 0, 1, 0);
				GlStateManager.translate((float) -blockpos.getX() - 0.5, (float) -blockpos.getY(), (float) -blockpos.getZ() - 0.5);
				GlStateManager.translate(currentX, -1, currentY);

				BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
				blockrendererdispatcher.getBlockModelRenderer().renderModel(te.getWorld(), blockrendererdispatcher.getModelForState(te.nextPair.getFirst()), te.nextPair.getFirst(), blockpos, vertexbuffer, false, 0);
				tessellator.draw();

				GlStateManager.enableLighting();
				GlStateManager.popMatrix();

			}*/
		}
	}
}
