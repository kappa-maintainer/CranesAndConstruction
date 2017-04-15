package me.lordsaad.cc.common.block;

import com.teamwizardry.librarianlib.common.base.block.BlockMod;
import me.lordsaad.cc.CCMain;
import me.lordsaad.cc.api.PosUtils;
import me.lordsaad.cc.api.SittingUtil;
import me.lordsaad.cc.init.ModBlocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
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
			BlockPos seat = PosUtils.findCraneSeat(worldIn, pos);
			if (seat != null) {
				boolean seated = SittingUtil.seatPlayer(worldIn, seat, playerIn);
				if (seated)
					playerIn.openGui(CCMain.instance, 0, worldIn, seat.getX(), seat.getY(), seat.getZ());
				return seated;
			}
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
