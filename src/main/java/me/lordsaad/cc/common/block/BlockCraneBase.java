package me.lordsaad.cc.common.block;

import com.teamwizardry.librarianlib.features.base.block.BlockMod;
import kotlin.Pair;
import me.lordsaad.cc.api.ILadder;
import me.lordsaad.cc.api.PosUtils;
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
import java.util.HashSet;

/**
 * Created by LordSaad.
 */
public class BlockCraneBase extends BlockMod implements ILadder {

	public BlockCraneBase() {
		super("crane_base", Material.IRON);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		//if (!worldIn.isRemote)
		//	PacketHandler.NETWORK.sendToAll(new PacketShowCraneParticles(pos, Color.GREEN));
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing heldItem, float side, float hitX, float hitY) {
		ItemStack item = playerIn.getHeldItem(hand);
		if (item.getItem() == new ItemStack(ModBlocks.CRANE_BASE).getItem()) {
			if (PosUtils.isBlockAtCraneBase(worldIn, pos)) {
				HashSet<BlockPos> vertical = PosUtils.getCraneVerticalPole(worldIn, pos, true, new HashSet<>());
				HashSet<BlockPos> horizontal = PosUtils.getCraneHorizontalPole(worldIn, pos);

				if (vertical == null) return false;
				HashSet<BlockPos> complete = new HashSet<>();
				complete.addAll(vertical);
				if (horizontal != null)
					complete.addAll(horizontal);
				HashSet<Pair<IBlockState, BlockPos>> structure = new HashSet<>();
				for (BlockPos block : complete) {
					IBlockState oldState = worldIn.getBlockState(block);
					structure.add(new Pair<>(oldState, block));
					if (block.toLong() != pos.toLong())
						worldIn.setBlockToAir(block);
				}

				for (Pair<IBlockState, BlockPos> pair : structure)
					worldIn.setBlockState(pair.getSecond().up(), pair.getFirst());

				if (!playerIn.isCreative()) item.setCount(item.getCount() - 1);

				//if (!worldIn.isRemote)
				//	PacketHandler.NETWORK.sendToAll(new PacketShowCraneParticles(pos, Color.GREEN));
				return true;
			}

		}
		return false;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		if (player.isSneaking()) return super.removedByPlayer(state, world, pos, player, willHarvest);
		HashSet<BlockPos> vertical = PosUtils.getCraneVerticalPole(world, pos, true, new HashSet<>());
		HashSet<BlockPos> horizontal = PosUtils.getCraneHorizontalPole(world, pos);

		if (vertical != null) {
			HashSet<BlockPos> complete = new HashSet<>();
			complete.addAll(vertical);
			if (horizontal != null)
				complete.addAll(horizontal);
			HashSet<Pair<IBlockState, BlockPos>> structure = new HashSet<>();
			for (BlockPos block : complete) {
				if (block.getY() <= pos.getY()) continue;
				IBlockState oldState = world.getBlockState(block);
				structure.add(new Pair<>(oldState, block));
				if (block.toLong() != pos.toLong())
					world.setBlockToAir(block);
			}

			if (structure.isEmpty()) return super.removedByPlayer(state, world, pos, player, willHarvest);

			for (Pair<IBlockState, BlockPos> pair : structure) {
				world.setBlockState(pair.getSecond().down(), pair.getFirst(), 3);
			}
			//if (!world.isRemote)
			//	PacketHandler.NETWORK.sendToAll(new PacketShowCraneParticles(pos, Color.GREEN));
			return false;
		}

		return super.removedByPlayer(state, world, pos, player, willHarvest);
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
