package me.lordsaad.cc.common.block;

import com.teamwizardry.librarianlib.client.fx.particle.ParticleBuilder;
import com.teamwizardry.librarianlib.client.fx.particle.ParticleSpawner;
import com.teamwizardry.librarianlib.client.fx.particle.functions.InterpFadeInOut;
import com.teamwizardry.librarianlib.common.base.block.BlockMod;
import com.teamwizardry.librarianlib.common.util.math.interpolate.StaticInterp;
import me.lordsaad.cc.CCMain;
import me.lordsaad.cc.api.PosUtils;
import me.lordsaad.cc.init.ModBlocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.HashSet;

/**
 * Created by LordSaad.
 */
public class BlockCraneBase extends BlockMod {

	public BlockCraneBase() {
		super("crane_base", Material.IRON);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing heldItem, float side, float hitX, float hitY) {
		ItemStack item = playerIn.getHeldItem(hand);
		if (item.getItem() == new ItemStack(ModBlocks.CRANE_BASE).getItem()) {
			IBlockState below = worldIn.getBlockState(pos.down());
			if (below.getBlock() != ModBlocks.CRANE_BASE && below.getBlock() != ModBlocks.CRANE_CORE) {
				PosUtils.placeUpwardShiftedBlocks(worldIn, PosUtils.getCrane(worldIn, pos, new HashSet<>()));
				worldIn.setBlockState(pos, ModBlocks.CRANE_BASE.getDefaultState());
			} else return false;
		} else {
			HashSet<BlockPos> vertical = PosUtils.getCraneVerticalPole(worldIn, pos, true, new HashSet<>());
			HashSet<BlockPos> horizontal = PosUtils.getCraneHorizontalPole(worldIn, pos);

			if (vertical != null)
				for (BlockPos block : vertical) {
					ParticleBuilder glitter = new ParticleBuilder(10);
					glitter.setRenderNormalLayer(new ResourceLocation(CCMain.MOD_ID, "particles/sparkle_blurred"));
					glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
					glitter.setColor(Color.GREEN);
					glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
					glitter.setScale(2);
					ParticleSpawner.spawn(glitter, worldIn, new StaticInterp<>(new Vec3d(block).addVector(0.5, 0.5, 0.5)), 1, 0, (aFloat, particleBuilder) -> {
					});
				}
			if (horizontal != null)
				for (BlockPos block : horizontal) {
					ParticleBuilder glitter = new ParticleBuilder(10);
					glitter.setRenderNormalLayer(new ResourceLocation(CCMain.MOD_ID, "particles/sparkle_blurred"));
					glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
					glitter.setColor(Color.RED);
					glitter.setAlphaFunction(new InterpFadeInOut(1f, 1f));
					glitter.setScale(2);
					ParticleSpawner.spawn(glitter, worldIn, new StaticInterp<>(new Vec3d(block).addVector(0.5, 0.5, 0.5)), 1, 0, (aFloat, particleBuilder) -> {
					});
				}
			/*BlockPos seat = PosUtils.findCraneSeat(worldIn, pos);
			if (seat != null) {
				boolean seated = SittingUtil.seatPlayer(worldIn, seat, playerIn);
				if (seated)
					playerIn.openGui(CCMain.instance, 0, worldIn, seat.getX(), seat.getY(), seat.getZ());
				return seated;
			}*/
		}
		return true;
	}


	@Override
	@SideOnly(Side.CLIENT)
	@Nonnull
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public boolean isOpaqueCube(IBlockState blockState) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
}
