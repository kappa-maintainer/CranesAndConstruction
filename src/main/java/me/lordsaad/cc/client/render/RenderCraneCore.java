package me.lordsaad.cc.client.render;

import com.teamwizardry.librarianlib.client.sprite.Sprite;
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
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;

/**
 * Created by LordSaad.
 */
public class RenderCraneCore extends TileEntitySpecialRenderer<TileCraneCore> {

	public static Sprite line = new Sprite(new ResourceLocation(CCMain.MOD_ID, "textures/blocks/crane_handle.png"));

	private IBakedModel modelCraneBase = null, modelCraneHandle;

	@Override
	public void renderTileEntityAt(TileCraneCore te, double x, double y, double z, float partialTicks, int destroyStage) {
		super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage);

		IModel model = null;
		if (modelCraneBase == null) {
			try {
				model = ModelLoaderRegistry.getModel(new ResourceLocation(CCMain.MOD_ID, "block/crane_base"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			modelCraneBase = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
					location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
		}
		if (modelCraneHandle == null) {
			try {
				model = ModelLoaderRegistry.getModel(new ResourceLocation(CCMain.MOD_ID, "block/crane_handle"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			modelCraneHandle = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
					location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
		}

		double subtractedMillis = (te.getWorld().getTotalWorldTime() - te.worldTime);
		double transitionTimeMax = Math.max(10, Math.min(Math.abs((te.prevYaw - te.destYaw) / 2.0), 35));
		float yaw;

		if (te.transitionArmToOrigin || te.transitionArm) {
			GlStateManager.pushMatrix();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.enableLighting();

			GlStateManager.translate(x + 0.5, y, z + 0.5);

			if (subtractedMillis < transitionTimeMax) {
				if (Math.round(te.destYaw) > Math.round(te.prevYaw))
					yaw = -((te.destYaw - te.prevYaw) / 2) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.destYaw + te.prevYaw) / 2;
				else
					yaw = ((te.prevYaw - te.destYaw) / 2) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.destYaw + te.prevYaw) / 2;
			} else yaw = te.destYaw;

			GlStateManager.rotate(yaw, 0, 1, 0);

			if (te.originalDirection == null) return;

			GlStateManager.translate(-0.5, 0, -0.5);
			for (int i = 1; i < te.armLength; i++) {
				BlockPos posOffset = BlockPos.ORIGIN.offset(te.originalDirection, i);
				GlStateManager.translate(posOffset.getX(), 0, posOffset.getZ());
				Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(modelCraneBase, 1.0F, 1, 1, 1);
				GlStateManager.translate(-posOffset.getX(), 0, -posOffset.getZ());
			}
			GlStateManager.popMatrix();

			/////////////////////////////
			//         HANDLE          //
			/////////////////////////////

			GlStateManager.pushMatrix();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.enableLighting();

			GlStateManager.translate(x + 0.5, y, z + 0.5);

			BlockPos initialOffset = te.getPos().offset(te.originalDirection.getOpposite());
			BlockPos adjustedY = new BlockPos(initialOffset.getX(), te.originalArmPos.getY(), initialOffset.getZ());
			BlockPos relative = te.getPos().subtract(adjustedY);
			float dist = (float) new Vec3d(te.handleFrom).distanceTo(new Vec3d(te.handleTo));
			int fixedY = (int) (dist - relative.getY() - 1);
			GlStateManager.translate(relative.getX(), -fixedY, relative.getZ());

			double currentX, currentY;
			if (subtractedMillis < transitionTimeMax) {
				if (Math.round(te.handleTo.getX()) > Math.round(te.handleFrom.getX()))
					currentX = -((te.handleTo.getX() - te.handleFrom.getX()) / 2) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.handleTo.getX() + te.handleFrom.getX()) / 2;
				else
					currentX = ((te.handleFrom.getX() - te.handleTo.getX()) / 2) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.handleTo.getX() + te.handleFrom.getX()) / 2;
			} else currentX = te.handleTo.getX();

			if (subtractedMillis < transitionTimeMax) {
				if (Math.round(te.handleTo.getY()) > Math.round(te.handleFrom.getY()))
					currentY = -((te.handleTo.getY() - te.handleFrom.getY()) / 2) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.handleTo.getY() + te.handleFrom.getY()) / 2;
				else
					currentY = ((te.handleFrom.getY() - te.handleTo.getY()) / 2) * MathHelper.cos((float) (subtractedMillis * Math.PI / transitionTimeMax)) + (te.handleTo.getY() + te.handleFrom.getY()) / 2;
			} else currentY = te.handleTo.getY();

			GlStateManager.rotate(yaw, 0, 1, 0);

			line.getTex().bind();
			line.draw((int) partialTicks, (float) -currentX, (float) -currentY, 0.3f, dist);

			GlStateManager.popMatrix();
		}
	}
}
