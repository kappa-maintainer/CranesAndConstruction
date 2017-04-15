package me.lordsaad.cc.client.gui;

import com.google.common.collect.HashMultimap;
import com.teamwizardry.librarianlib.client.core.ClientTickHandler;
import com.teamwizardry.librarianlib.client.gui.GuiBase;
import com.teamwizardry.librarianlib.client.gui.GuiComponent;
import com.teamwizardry.librarianlib.client.gui.components.ComponentSprite;
import com.teamwizardry.librarianlib.client.gui.components.ComponentVoid;
import com.teamwizardry.librarianlib.client.sprite.Sprite;
import com.teamwizardry.librarianlib.client.sprite.Texture;
import me.lordsaad.cc.CCMain;
import me.lordsaad.cc.api.PosUtils;
import me.lordsaad.cc.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by LordSaad.
 */
public class GuiCrane extends GuiBase {

	Texture textureBackground = new Texture(new ResourceLocation(CCMain.MOD_ID, "textures/gui/crane_gui.png"));
	Sprite spriteBackground = textureBackground.getSprite("bg", 165, 256);
	Sprite tileSelector = new Sprite(new ResourceLocation(CCMain.MOD_ID, "textures/gui/tile_select.png"));

	private static Minecraft mc = Minecraft.getMinecraft();
	private double tick = 0;
	private IBlockState[][] grid;
	private HashMultimap<IBlockState, BlockPos> blocks = HashMultimap.create();

	public GuiCrane(BlockPos pos) {
		super(330, 512);

		int width = 0;
		int height = 0;

		Set<BlockPos> cranePoses = PosUtils.getCrane(mc.world, pos, new HashSet<>());
		BlockPos farthest = null;
		for (BlockPos place : cranePoses) {
			if (farthest == null) farthest = place;
			else {
				int tempdist = (int) place.getDistance(pos.getX(), pos.getY(), pos.getZ());
				if (tempdist > width) {
					farthest = place;
					height = width = tempdist;
				}
			}
		}
		width = Math.min(width, 11);
		height = Math.min(height, 13);

		for (int i = -width; i < width; i++)
			for (int j = -width; j < width; j++)
				for (int k = -height; k < 1; k++) {
					BlockPos pos1 = new BlockPos(pos.getX() + i, pos.getY() + k, pos.getZ() + j);
					if (mc.world.isAirBlock(pos1)) continue;
					IBlockState state = mc.world.getBlockState(pos1);
					if (mc.world.canBlockSeeSky(pos1)
							//|| mc.world.canBlockSeeSky(pos1.offset(EnumFacing.UP))
							|| state.getBlock() == ModBlocks.CRANE_CORE
							|| state.getBlock() == ModBlocks.CRANE_BASE)
						blocks.put(state, pos1.subtract(pos));
				}

		ComponentSprite compBackground = new ComponentSprite(spriteBackground, 0, 0, 330, 512);
		getMainComponents().add(compBackground);

		ComponentVoid isometricRenderer = new ComponentVoid(15, 10, 150 * 2, 88 * 2);
		isometricRenderer.BUS.hook(GuiComponent.PostDrawEvent.class, (event) -> {

			if (tick >= 360) tick = 0;
			else tick++;

			int horizontalAngle = 40;
			int verticalAngle = 45;

			for (IBlockState state : blocks.keySet())
				for (BlockPos pos1 : blocks.get(state)) {

					GlStateManager.pushMatrix();
					GlStateManager.disableCull();

					int gridScale = 9;

					GlStateManager.translate(165, 60, 500);
					GlStateManager.rotate((float) ((tick + event.getPartialTicks())), 0, 1, 0);
					GlStateManager.translate(pos1.getX() * gridScale, -pos1.getY() * gridScale, pos1.getZ() * gridScale);
					GlStateManager.scale(gridScale, gridScale, gridScale);

					GlStateManager.translate(-pos1.getX(), -pos1.getY(), -pos1.getZ());

					Tessellator tes = Tessellator.getInstance();
					VertexBuffer buffer = tes.getBuffer();
					BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

					mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

					dispatcher.getBlockModelRenderer().renderModelFlat(mc.world, dispatcher.getModelForState(state), state, pos1, buffer, false, 0);

					tes.draw();

					GlStateManager.popMatrix();
				}
		});

		grid = new IBlockState[width * 2][width * 2];
		ComponentVoid topView = new ComponentVoid(15, 200, 150 * 2, 150 * 2);
		int finalWidth = width;
		int finalHeight = height;
		topView.BUS.hook(GuiComponent.PostDrawEvent.class, (event) -> {
			if (tick >= 360) tick = 0;
			else tick++;

			int horizontalAngle = 90;

			for (IBlockState state : blocks.keySet())
				for (BlockPos pos1 : blocks.get(state)) {

					GlStateManager.pushMatrix();
					GlStateManager.disableCull();

					double gridScale = 13.5;
					Minecraft.getMinecraft().player.sendChatMessage(finalWidth + "");

					GlStateManager.translate(166, 200 + (149), 500);
					GlStateManager.rotate(-horizontalAngle, 1, 0, 0);
					GlStateManager.translate(pos1.getX() * gridScale, -pos1.getY() * gridScale, pos1.getZ() * gridScale);
					GlStateManager.scale(gridScale, gridScale, gridScale);

					GlStateManager.translate(-pos1.getX(), -pos1.getY(), -pos1.getZ());

					Tessellator tes = Tessellator.getInstance();
					VertexBuffer buffer = tes.getBuffer();
					BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

					mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

					dispatcher.getBlockModelRenderer().renderModelFlat(mc.world, dispatcher.getModelForState(state), state, pos1, buffer, false, 0);

					tes.draw();

					GlStateManager.popMatrix();

					GlStateManager.pushMatrix();
					int tile = 9;
					int tileHalf = tile / 2;
					int x = (int) (event.getMousePos().getX() / 16);
					int y = (int) (event.getMousePos().getY() / 16);

					GlStateManager.translate(x * tile, y * tile, 1000);
					tileSelector.getTex().bind();
					tileSelector.draw((int) ClientTickHandler.getPartialTicks(), 19, 200);
					GlStateManager.popMatrix();
				}
		});

		topView.BUS.hook(GuiComponent.MouseClickEvent.class, (event) -> {

		});

		compBackground.add(isometricRenderer);
		compBackground.add(topView);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
