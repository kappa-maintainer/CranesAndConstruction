package me.lordsaad.cc.client.gui;

import com.google.common.collect.HashMultimap;
import com.teamwizardry.librarianlib.core.client.ClientTickHandler;
import com.teamwizardry.librarianlib.features.gui.GuiBase;
import com.teamwizardry.librarianlib.features.gui.GuiComponent;
import com.teamwizardry.librarianlib.features.gui.components.*;
import com.teamwizardry.librarianlib.features.gui.mixin.ButtonMixin;
import com.teamwizardry.librarianlib.features.gui.mixin.ScissorMixin;
import com.teamwizardry.librarianlib.features.gui.mixin.gl.GlMixin;
import com.teamwizardry.librarianlib.features.kotlin.ClientUtilMethods;
import com.teamwizardry.librarianlib.features.math.Vec2d;
import com.teamwizardry.librarianlib.features.network.PacketHandler;
import com.teamwizardry.librarianlib.features.sprite.Sprite;
import com.teamwizardry.librarianlib.features.sprite.Texture;
import kotlin.Pair;
import me.lordsaad.cc.CCMain;
import me.lordsaad.cc.api.PosUtils;
import me.lordsaad.cc.common.network.PacketReduceStackFromPlayer;
import me.lordsaad.cc.common.network.PacketSendBlockToCrane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;

/**
 * Created by LordSaad.
 */
public class GuiCrane extends GuiBase {

	private static Minecraft mc = Minecraft.getMinecraft();
	Texture textureBackground = new Texture(new ResourceLocation(CCMain.MOD_ID, "textures/gui/crane_gui.png"));
	Sprite spriteBackground = textureBackground.getSprite("bg", 245, 256);
	Sprite tileSelector = new Sprite(new ResourceLocation(CCMain.MOD_ID, "textures/gui/tile_select.png"));
	Sprite tileSelector2 = new Sprite(new ResourceLocation(CCMain.MOD_ID, "textures/gui/tile_select_2.png"));
	private double tick = 0;
	private HashMultimap<IBlockState, BlockPos> blocks = HashMultimap.create();
	private ComponentStack selected;
	private ComponentSprite selectionRect = new ComponentSprite(tileSelector, 0, 0, 32, 32);
	private ComponentSprite hoverRect = new ComponentSprite(tileSelector, 0, 0, 32, 32);

	private Vec2d offset, from;
	private int prevX = 0, prevY = 0;

	private int[] vbocache1 = null, vbocache2 = null;

	public GuiCrane(BlockPos pos) {
		super(490, 512);

		selectionRect.setVisible(false);
		hoverRect.setVisible(false);
		getMainComponents().add(selectionRect, hoverRect);

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

		int extraHeight = ((highestBlock == null || craneSeat == null) ? 1 : Math.abs(highestBlock.getY() - craneSeat.getY()) + 1);

		for (int i = -width; i < width; i++)
			for (int j = -width; j < width; j++)
				for (int k = -height - extraHeight; k < extraHeight; k++) {
					BlockPos pos1 = new BlockPos(pos.getX() + i, pos.getY() + k, pos.getZ() + j);
					if (mc.world.isAirBlock(pos1)) continue;

					IBlockState state = mc.world.getBlockState(pos1);
					int sky = mc.world.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos1);
					int block = mc.world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos1);
					boolean surrounded = true;
					for (EnumFacing facing : EnumFacing.VALUES)
						if (mc.world.isAirBlock(pos1.offset(facing))) {
							surrounded = false;
							break;
						}
					if (Math.max(sky, block) >= 15 || !surrounded) {
						blocks.put(state, pos1.subtract(pos));
					}
				}

		ComponentSprite compBackground = new ComponentSprite(spriteBackground, 0, 0, 490, 512);
		getMainComponents().add(compBackground);

		ComponentVoid boxing2 = new ComponentVoid(175, 10, 150 * 2, 88 * 2);
		ComponentVoid sideView = new ComponentVoid(0, 0, 150 * 2, 88 * 2);

		boxing2.add(sideView);
		ScissorMixin.INSTANCE.scissor(sideView);

		int guiSideWidth = 70 * 2;
		int tileSideSize = guiSideWidth / (height * 2);
		sideView.BUS.hook(GuiComponent.PostDrawEvent.class, (event) -> {

			if (tick >= 360 * 2) tick = 0;
			else tick++;

			int horizontalAngle = 40;
			int verticalAngle = 45;

			GlStateManager.pushMatrix();
			GlStateManager.disableCull();

			GlStateManager.translate(150, 75, 500);
			GlStateManager.rotate(180, 1, 0, 0);
			GlStateManager.rotate(horizontalAngle, -1, 0, 0);
			GlStateManager.rotate((float) ((tick + event.getPartialTicks()) / 2), 0, 1, 0);
			GlStateManager.translate(tileSideSize, tileSideSize, tileSideSize);
			GlStateManager.scale(tileSideSize, tileSideSize, tileSideSize);

			Tessellator tes = Tessellator.getInstance();
			VertexBuffer buffer = tes.getBuffer();
			BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

			if (vbocache2 == null) { // if there is no cache, create one
				buffer.begin(7, DefaultVertexFormats.BLOCK); // init the buffer with the settings

				for (IBlockState state2 : blocks.keySet())
					for (BlockPos pos2 : blocks.get(state2))
						dispatcher.getBlockModelRenderer().renderModelFlat(mc.world, dispatcher.getModelForState(state2), state2, pos2, buffer, false, 0);

				vbocache2 = ClientUtilMethods.createCacheArrayAndReset(buffer); // cache your values
			}

			// once that’s done, draw the cache
			mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

			buffer.begin(7, DefaultVertexFormats.BLOCK);
			buffer.addVertexData(vbocache2);

			tes.draw();

			GlStateManager.popMatrix();
		});

		getMainComponents().add(boxing2);

		int tileSize = 16;

		ComponentVoid boxing = new ComponentVoid(175, 200, 300, 300);
		ComponentRect topView = new ComponentRect(0, 0, 300, 300);

		boxing.add(topView);
		getMainComponents().add(boxing);
		ScissorMixin.INSTANCE.scissor(topView);

		topView.BUS.hook(GuiComponent.PostDrawEvent.class, (event) -> {
			GlStateManager.pushMatrix();
			GlStateManager.disableCull();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.enableLighting();

			GlStateManager.translate(133, 133, 200);
			if (offset != null)
				GlStateManager.translate(-offset.getX(), -offset.getY(), 0);
			GlStateManager.rotate(-90, 1, 0, 0);

			Tessellator tes = Tessellator.getInstance();
			VertexBuffer buffer = tes.getBuffer();
			BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

			if (vbocache1 == null) { // if there is no cache, create one
				buffer.begin(7, DefaultVertexFormats.BLOCK); // init the buffer with the settings

				for (IBlockState state : blocks.keySet())
					for (BlockPos pos1 : blocks.get(state))
						dispatcher.getBlockModelRenderer().renderModelFlat(mc.world, dispatcher.getModelForState(state), state, pos1, buffer, false, 0);

				vbocache1 = ClientUtilMethods.createCacheArrayAndReset(buffer); // cache your values
			}

			// once that’s done, draw the cache
			mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.scale(tileSize, tileSize, tileSize);
			//GlStateManager.translate(-centerPos.x, -centerPos.y, -centerPos.z);

			buffer.begin(7, DefaultVertexFormats.BLOCK);
			buffer.addVertexData(vbocache1);

			tes.draw();

			GlStateManager.popMatrix();

			if (!event.getComponent().getMouseOver()) return;
			int gridX = event.getMousePos().getXi() / tileSize * tileSize;
			int gridY = event.getMousePos().getYi() / tileSize * tileSize;

			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 1000);
			tileSelector2.getTex().bind();
			tileSelector2.draw((int) ClientTickHandler.getPartialTicks(), gridX + 5, gridY + 5, tileSize, tileSize);
			GlStateManager.popMatrix();
		});


		new ButtonMixin<>(topView, () -> {
		});

		topView.BUS.hook(GuiComponent.MouseDragEvent.class, (event) -> {
			if (!event.getComponent().getMouseOver()) return;
			if (!isShiftKeyDown()) {
				int x = event.getMousePos().getXi() / tileSize;
				int y = event.getMousePos().getYi() / tileSize;
				if (x == prevX && y == prevY) return;
				else {
					prevX = x;
					prevY = y;
				}

				BlockPos block = pos.add(new BlockPos(x, 0, y));
				if (selected != null) {
					ItemBlock itemBlock = (ItemBlock) selected.getStack().getValue(selected).getItem();
					PacketHandler.NETWORK.sendToServer(new PacketSendBlockToCrane(pos, new Pair<>(itemBlock.block.getDefaultState(), block)));
					if (!mc.player.isCreative())
						PacketHandler.NETWORK.sendToServer(new PacketReduceStackFromPlayer(mc.player.inventory.getSlotFor(selected.getStack().getValue(selected))));
				}
			} else {
				if (from != null) {
					offset = from.sub(event.getMousePos());
				}
			}
		});

		topView.BUS.hook(GuiComponent.MouseDownEvent.class, (event) -> {
			if (!event.getComponent().getMouseOver()) return;
			if (!isShiftKeyDown()) {

				if (!event.getComponent().getMouseOver()) return;
				double x = event.getMousePos().getXi() / tileSize;
				double y = event.getMousePos().getYi() / tileSize;
				BlockPos block = pos.add(new BlockPos(x, 0, y));

				if (selected != null) {
					ItemBlock itemBlock = (ItemBlock) selected.getStack().getValue(selected).getItem();
					PacketHandler.NETWORK.sendToServer(new PacketSendBlockToCrane(pos, new Pair<>(itemBlock.block.getDefaultState(), block)));
					if (!mc.player.isCreative())
						PacketHandler.NETWORK.sendToServer(new PacketReduceStackFromPlayer(mc.player.inventory.getSlotFor(selected.getStack().getValue(selected))));
				}
			} else {
				if (offset == null) from = event.getMousePos();
				else from = event.getMousePos().add(offset);
			}
		});

		Deque<ItemStack> itemBlocks = new ArrayDeque<>();
		for (ItemStack stack : mc.player.inventory.mainInventory)
			if (stack.getItem() instanceof ItemBlock)
				itemBlocks.add(stack);

		final int size = itemBlocks.size();
		for (int i = 0; i < Math.ceil(size / 9.0); i++) {
			ComponentList inventory = new ComponentList(16 + (i * 36), 16);
			inventory.setChildScale(2);

			for (int j = 0; j < 9; j++) {
				if (itemBlocks.isEmpty()) break;
				ItemStack stack = itemBlocks.pop();
				ComponentStack compStack = new ComponentStack(0, 0);
				compStack.setMarginBottom(2);
				compStack.getStack().setValue(stack);

				final int finalI = i, finalJ = j;
				compStack.BUS.hook(GuiComponent.MouseClickEvent.class, (event) -> {
					if (!event.getComponent().getMouseOver()) return;
					selected = compStack;
					selectionRect.setVisible(true);
					selectionRect.setPos(new Vec2d(16 + finalI * 36, 16 + finalJ * 36));
					GlMixin.INSTANCE.transform(selectionRect).setValue(new Vec3d(0, 0, 100));
				});

				compStack.BUS.hook(GuiComponent.MouseOverEvent.class, (event) -> {
					if (!event.getComponent().getMouseOver()) return;
					hoverRect.setVisible(true);
					hoverRect.setPos(new Vec2d(16 + finalI * 36, 16 + finalJ * 36));
					GlMixin.INSTANCE.transform(hoverRect).setValue(new Vec3d(0, 0, 100));
				});
				inventory.add(compStack);
			}
			getMainComponents().add(inventory);
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
