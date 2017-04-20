package me.lordsaad.cc.client.gui;

import com.google.common.collect.HashMultimap;
import com.teamwizardry.librarianlib.client.core.ClientTickHandler;
import com.teamwizardry.librarianlib.client.fx.particle.ParticleBuilder;
import com.teamwizardry.librarianlib.client.fx.particle.ParticleSpawner;
import com.teamwizardry.librarianlib.client.fx.particle.functions.InterpFadeInOut;
import com.teamwizardry.librarianlib.client.gui.GuiBase;
import com.teamwizardry.librarianlib.client.gui.GuiComponent;
import com.teamwizardry.librarianlib.client.gui.components.ComponentList;
import com.teamwizardry.librarianlib.client.gui.components.ComponentSprite;
import com.teamwizardry.librarianlib.client.gui.components.ComponentStack;
import com.teamwizardry.librarianlib.client.gui.components.ComponentVoid;
import com.teamwizardry.librarianlib.client.gui.mixin.ButtonMixin;
import com.teamwizardry.librarianlib.client.sprite.Sprite;
import com.teamwizardry.librarianlib.client.sprite.Texture;
import com.teamwizardry.librarianlib.common.network.PacketHandler;
import com.teamwizardry.librarianlib.common.util.math.interpolate.StaticInterp;
import kotlin.Pair;
import me.lordsaad.cc.CCMain;
import me.lordsaad.cc.api.PosUtils;
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
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.EnumSkyBlock;
import org.lwjgl.opengl.GL11;

import java.awt.*;
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
	private double tick = 0;
	private IBlockState[][] grid;
	private HashMultimap<IBlockState, BlockPos> blocks = HashMultimap.create();
	private ComponentStack selected;

	public GuiCrane(BlockPos pos) {
		super(490, 512);

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
				for (int k = -height - extraHeight + (width > 15 ? width / 10 : 0); k < extraHeight; k++) {
					BlockPos pos1 = new BlockPos(pos.getX() + i, pos.getY() + k, pos.getZ() + j);
					if (mc.world.isAirBlock(pos1)) continue;
					IBlockState state = mc.world.getBlockState(pos1);
					int sky = mc.world.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos);
					int block = mc.world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos);
					if (Math.max(sky, block) >= 15) {
						boolean surrounded = true;
						for (EnumFacing facing : EnumFacing.VALUES)
							if (mc.world.isAirBlock(pos1.offset(facing))) {
								surrounded = false;
								break;
							}
						if (!surrounded)
							blocks.put(state, pos1.subtract(pos));
					}
				}

		ComponentSprite compBackground = new ComponentSprite(spriteBackground, 0, 0, 490, 512);
		getMainComponents().add(compBackground);

		ComponentVoid sideView = new ComponentVoid(175, 10, 150 * 2, 88 * 2);
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

					GlStateManager.translate(325, 50, 500);
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
		ComponentVoid topView = new ComponentVoid(175, 200, 300, 298);
		topView.BUS.hook(GuiComponent.PostDrawEvent.class, (event) -> {
			for (IBlockState state : blocks.keySet())
				for (BlockPos pos1 : blocks.get(state)) {

					GlStateManager.pushMatrix();
					GlStateManager.disableCull();
					GlStateManager.disableLighting();

					GlStateManager.translate(325, 349, 300);
					GlStateManager.rotate(-90, 1, 0, 0);
					GlStateManager.translate(pos1.getX() * tileSize, -pos1.getY() * tileSize, pos1.getZ() * tileSize);
					GlStateManager.scale(tileSize, tileSize, tileSize);

					GlStateManager.translate(-pos1.getX(), -pos1.getY(), -pos1.getZ());

					Tessellator tes = Tessellator.getInstance();
					VertexBuffer buffer = tes.getBuffer();
					BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

					mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					buffer.begin(7, DefaultVertexFormats.BLOCK);

					dispatcher.getBlockModelRenderer().renderModel(mc.world, dispatcher.getModelForState(state), state, pos1, buffer, false, 0);

					tes.draw();

					GlStateManager.enableLighting();
					GlStateManager.popMatrix();
				}

			if (!event.getComponent().getMouseOver()) return;
			int gridX = event.getMousePos().getXi() / tileSize;
			int gridY = event.getMousePos().getYi() / tileSize;

			GlStateManager.pushMatrix();
			GlStateManager.translate(180, 200, 500);
			tileSelector.getTex().bind();
			tileSelector.draw((int) ClientTickHandler.getPartialTicks(), gridX * tileSize, gridY * tileSize, tileSize, tileSize);
			GlStateManager.popMatrix();
		});


		new ButtonMixin<>(topView, () -> {
		});

		topView.BUS.hook(GuiComponent.MouseDragEvent.class, (event) -> {
			if (!event.getComponent().getMouseOver()) return;
			int x = event.getMousePos().getXi() / tileSize;
			int y = event.getMousePos().getYi() / tileSize;
			BlockPos block = pos.subtract(new Vec3i(width, 0, width)).add(x, 0, y);

			ParticleBuilder glitter = new ParticleBuilder(40);
			glitter.setRenderNormalLayer(new ResourceLocation(CCMain.MOD_ID, "particles/sparkle_blurred"));
			glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
			glitter.setColor(Color.GREEN);
			glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
			glitter.setScale(2);
			ParticleSpawner.spawn(glitter, mc.world, new StaticInterp<>(new Vec3d(block).addVector(0.5, 0.5, 0.5)), 1, 0, (aFloat, particleBuilder) -> {
			});

			if (selected != null) {
				ItemBlock itemBlock = (ItemBlock) selected.getStack().getValue(selected).getItem();
				PacketHandler.NETWORK.sendToServer(new PacketSendBlockToCrane(pos, new Pair<>(itemBlock.block.getDefaultState(), block)));
			}
		});

		topView.BUS.hook(GuiComponent.MouseDownEvent.class, (event) -> {
			if (!event.getComponent().getMouseOver()) return;
			int x = event.getMousePos().getXi() / tileSize;
			int y = event.getMousePos().getYi() / tileSize;
			BlockPos block = pos.subtract(new Vec3i(width, 0, width)).add(x, 0, y);

			ParticleBuilder glitter = new ParticleBuilder(40);
			glitter.setRenderNormalLayer(new ResourceLocation(CCMain.MOD_ID, "particles/sparkle_blurred"));
			glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
			glitter.setColor(Color.GREEN);
			glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
			glitter.setScale(2);
			ParticleSpawner.spawn(glitter, mc.world, new StaticInterp<>(new Vec3d(block).addVector(0.5, 0.5, 0.5)), 1, 0, (aFloat, particleBuilder) -> {
			});

			if (selected != null) {
				ItemBlock itemBlock = (ItemBlock) selected.getStack().getValue(selected).getItem();
				PacketHandler.NETWORK.sendToServer(new PacketSendBlockToCrane(pos, new Pair<>(itemBlock.block.getDefaultState(), block)));
			}
		});

		Deque<ItemStack> itemBlocks = new ArrayDeque<>();
		for (ItemStack stack : mc.player.inventory.mainInventory)
			if (stack.getItem() instanceof ItemBlock)
				itemBlocks.add(stack);

		final int size = itemBlocks.size();
		for (int i = 0; i < Math.ceil(size / 9.0); i++) {
			ComponentList inventory = new ComponentList(124 - (i * 36), 15);
			inventory.setChildScale(2);

			for (int j = 0; j < 9; j++) {
				if (itemBlocks.isEmpty()) break;
				ItemStack stack = itemBlocks.pop();
				ComponentStack compStack = new ComponentStack(0, 0);
				compStack.setMarginBottom(2);
				compStack.getStack().setValue(stack);

				compStack.BUS.hook(GuiComponent.MouseClickEvent.class, (event) -> {
					selected = compStack;
				});

				int finalI = i;
				int finalJ = j;
				compStack.BUS.hook(GuiComponent.PreDrawEvent.class, (event) -> {
					if (selected == compStack || event.getComponent().getMouseOver()) {
						GlStateManager.pushMatrix();
						GlStateManager.enableAlpha();
						GlStateManager.enableBlend();

						if (event.getComponent().getMouseOver() && selected != compStack)
							GlStateManager.color(1f, 1f, 1f, 0.75f);

						tileSelector.getTex().bind();
						tileSelector.draw((int) ClientTickHandler.getPartialTicks(), 0.5f, 0.5f + (tileSize * finalJ * 3.6f), 15.5f, 15.5f);

						GlStateManager.popMatrix();
					}
				});
				inventory.add(compStack);
			}
			getMainComponents().add(inventory);
		}

		compBackground.add(sideView, topView);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
