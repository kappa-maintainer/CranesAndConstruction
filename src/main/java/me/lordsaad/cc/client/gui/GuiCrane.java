package me.lordsaad.cc.client.gui;

import com.google.common.collect.HashMultimap;
import com.teamwizardry.librarianlib.client.core.ClientTickHandler;
import com.teamwizardry.librarianlib.client.gui.GuiBase;
import com.teamwizardry.librarianlib.client.gui.GuiComponent;
import com.teamwizardry.librarianlib.client.gui.components.ComponentSprite;
import com.teamwizardry.librarianlib.client.gui.components.ComponentVoid;
import com.teamwizardry.librarianlib.client.gui.mixin.ButtonMixin;
import com.teamwizardry.librarianlib.client.sprite.Sprite;
import com.teamwizardry.librarianlib.client.sprite.Texture;
import com.teamwizardry.librarianlib.common.util.math.Vec2d;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;

/**
 * Created by LordSaad.
 */
public class GuiCrane extends GuiBase {

	private static Minecraft mc = Minecraft.getMinecraft();
	Texture textureBackground = new Texture(new ResourceLocation(CCMain.MOD_ID, "textures/gui/crane_gui.png"));
	Sprite spriteBackground = textureBackground.getSprite("bg", 165, 256);
	Sprite tileSelector = new Sprite(new ResourceLocation(CCMain.MOD_ID, "textures/gui/tile_select.png"));
	private double tick = 0;
	private IBlockState[][] grid;
	private HashMultimap<IBlockState, BlockPos> blocks = HashMultimap.create();

	public GuiCrane(BlockPos pos) {
		super(330, 512);

		int width;
		int height;

		HashSet<BlockPos> vertical = PosUtils.getCraneVerticalPole(mc.world, pos, true, new HashSet<>());
		HashSet<BlockPos> horizontal = PosUtils.getCraneHorizontalPole(mc.world, pos);
		BlockPos highestBlock = PosUtils.getHighestCranePoint(vertical);
		BlockPos craneSeat = PosUtils.findCraneSeat(mc.world, pos);

		if (horizontal == null) width = 0;
		else width = horizontal.size() - 1;

		if (vertical == null) height = 0;
		else height = vertical.size() - 1;

		if (height == 0 || width == 0) return;

		int extraHeight = (highestBlock == null || craneSeat == null ? 1 : Math.abs(highestBlock.getY() - craneSeat.getY()) + 1);

		for (int i = -width; i < width; i++)
			for (int j = -width; j < width; j++)
				for (int k = -height; k < extraHeight; k++) {
					BlockPos pos1 = new BlockPos(pos.getX() + i, pos.getY() + k, pos.getZ() + j);
					if (mc.world.isAirBlock(pos1)) continue;
					IBlockState state = mc.world.getBlockState(pos1);
					if (mc.world.getCombinedLight(pos1, 15) >= 10
							|| state.getBlock() == ModBlocks.CRANE_CORE
							|| state.getBlock() == ModBlocks.CRANE_BASE)
						blocks.put(state, pos1.subtract(pos));
				}

		ComponentSprite compBackground = new ComponentSprite(spriteBackground, 0, 0, 330, 512);
		getMainComponents().add(compBackground);

		ComponentVoid sideView = new ComponentVoid(15, 10, 150 * 2, 88 * 2);
		int guiSideWidth = 70 * 2;
		int tileSideSize = guiSideWidth / (height * 2);
		sideView.BUS.hook(GuiComponent.PostDrawEvent.class, (event) -> {

			if (tick >= 360) tick = 0;
			else tick++;

			int horizontalAngle = 40;
			int verticalAngle = 45;

			for (IBlockState state : blocks.keySet())
				for (BlockPos pos1 : blocks.get(state)) {

					GlStateManager.pushMatrix();
					GlStateManager.disableCull();

					GlStateManager.translate(165, 50, 500);
					GlStateManager.rotate((float) ((tick + event.getPartialTicks())), 0, 1, 0);
					GlStateManager.translate(pos1.getX() * tileSideSize, -pos1.getY() * tileSideSize, pos1.getZ() * tileSideSize);
					GlStateManager.scale(tileSideSize, tileSideSize, tileSideSize);

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
		int guiSize = 300;
		int tileSize = guiSize / (width * 2);
		ComponentVoid topView = new ComponentVoid(15, 200, 300, 298);
		topView.BUS.hook(GuiComponent.PostDrawEvent.class, (event) -> {
			for (IBlockState state : blocks.keySet())
				for (BlockPos pos1 : blocks.get(state)) {

					GlStateManager.pushMatrix();
					GlStateManager.disableCull();

					GlStateManager.translate(166, 349, 300);
					GlStateManager.rotate(-90, 1, 0, 0);
					GlStateManager.translate(pos1.getX() * tileSize, -pos1.getY() * tileSize, pos1.getZ() * tileSize);
					GlStateManager.scale(tileSize, tileSize, tileSize);

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

			if (!event.getComponent().getMouseOver()) return;
			int gridX = event.getMousePos().getXi() / tileSize;
			int gridY = event.getMousePos().getYi() / tileSize;

			GlStateManager.pushMatrix();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.translate(16, 199, 500);
			tileSelector.getTex().bind();
			tileSelector.draw((int) ClientTickHandler.getPartialTicks(), gridX * tileSize, gridY * tileSize, tileSize, tileSize);
			GlStateManager.popMatrix();
		});


		new ButtonMixin<>(topView, () -> {
		});

		topView.BUS.hook(GuiComponent.MouseDragEvent.class, (event) -> {
			Vec2d pos1 = event.getMousePos();
			int x = pos1.getXi() / 16;
			int y = pos1.getYi() / 16;
			//if (x < grid.length && y < grid.length && x > 0 && y > 0)
			//	if (event.getButton() == EnumMouseButton.LEFT)
			//		grid[x][y] = TileType.PLACED;
			//	else if (event.getButton() == EnumMouseButton.RIGHT)
			//		grid[x][y] = TileType.EMPTY;
			//PacketHandler.NETWORK.sendToServer(new PacketBuilderGridSaver(location, grid));

		});

		topView.BUS.hook(GuiComponent.MouseDownEvent.class, (event) -> {
			Vec2d pos1 = event.getMousePos();
			int x = pos1.getXi() / 16;
			int y = pos1.getYi() / 16;
			if (x < grid.length && y < grid.length && x > 0 && y > 0) ;
			//if (selectedMode == Mode.DIRECT) {
			//	if (grid[x][y] == TileType.EMPTY)
			//		grid[x][y] = TileType.PLACED;
			//	else grid[x][y] = TileType.EMPTY;
			//	PacketHandler.NETWORK.sendToServer(new PacketBuilderGridSaver(location, grid));
			//} else if (selectedMode == Mode.SELECT) {
			//	if (grid[x][y] == TileType.EMPTY)
			//		if (event.getButton() == EnumMouseButton.LEFT) {
			//			Vec2d left = getTile(TileType.LEFT_SELECTED);
			//			if (left != null) grid[left.getXi()][left.getYi()] = TileType.EMPTY;
			//			grid[x][y] = TileType.LEFT_SELECTED;
			//			PacketHandler.NETWORK.sendToServer(new PacketBuilderGridSaver(location, grid));
			//		} else {
			//			Vec2d left = getTile(TileType.RIGHT_SELECTED);
			//			if (left != null) grid[left.getXi()][left.getYi()] = TileType.EMPTY;
			//			grid[x][y] = TileType.RIGHT_SELECTED;
			//			PacketHandler.NETWORK.sendToServer(new PacketBuilderGridSaver(location, grid));
			//		}
			//	else {
			//		grid[x][y] = TileType.EMPTY;
			//		PacketHandler.NETWORK.sendToServer(new PacketBuilderGridSaver(location, grid));
			//	}
			//}
		});


		compBackground.add(sideView);
		compBackground.add(topView);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
