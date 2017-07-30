package me.lordsaad.cc.common.block;

import com.teamwizardry.librarianlib.features.base.block.BlockMod;
import com.teamwizardry.librarianlib.features.network.PacketHandler;
import me.lordsaad.cc.api.CraneManager;
import me.lordsaad.cc.api.ILadder;
import me.lordsaad.cc.api.PosUtils;
import me.lordsaad.cc.common.network.PacketShowCraneParticles;
import me.lordsaad.cc.init.ModBlocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
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

/**
 * Created by LordSaad.
 */
public class BlockScaffolding extends BlockMod implements ILadder {

	public BlockScaffolding() {
		super("scaffolding", Material.IRON);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		if (!worldIn.isRemote)
			PacketHandler.NETWORK.sendToAll(new PacketShowCraneParticles(pos));
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing heldItem, float side, float hitX, float hitY) {
		ItemStack item = playerIn.getHeldItem(hand);
		if (item.getItem() == new ItemStack(ModBlocks.SCAFFOLDING).getItem()) {
			CraneManager manager = new CraneManager(worldIn, pos);

			if (manager.bottomBlock != null && manager.bottomBlock.getY() == pos.getY()) {

				manager.elongateCrane(pos);

				if (!playerIn.isCreative()) item.shrink(1);

				if (!worldIn.isRemote)
					PacketHandler.NETWORK.sendToAll(new PacketShowCraneParticles(pos));
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		if (!world.isRemote)
			PacketHandler.NETWORK.sendToAll(new PacketShowCraneParticles(pos));

		if (player.isSneaking()) return super.removedByPlayer(state, world, pos, player, willHarvest);

		CraneManager manager = new CraneManager(world, pos);

		if (PosUtils.isPosInSet(pos, manager.arm)
				|| (manager.highestBlock != null && manager.highestBlock.getY() == pos.getY())
				|| manager.bottomBlock == null
				|| !CraneManager.hasPole(world, pos)) {
			return super.removedByPlayer(state, world, pos, player, willHarvest);
		}
		manager.shrinkCrane(pos);

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@Nonnull
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
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
