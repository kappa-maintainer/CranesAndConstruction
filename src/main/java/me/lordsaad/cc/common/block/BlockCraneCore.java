package me.lordsaad.cc.common.block;

import com.teamwizardry.librarianlib.common.base.block.BlockModContainer;
import com.teamwizardry.librarianlib.common.network.PacketHandler;
import kotlin.Pair;
import me.lordsaad.cc.CCMain;
import me.lordsaad.cc.api.ILadder;
import me.lordsaad.cc.api.PosUtils;
import me.lordsaad.cc.api.SittingUtil;
import me.lordsaad.cc.client.render.RenderCraneCore;
import me.lordsaad.cc.common.network.PacketShowCraneParticles;
import me.lordsaad.cc.common.tile.TileCraneCore;
import me.lordsaad.cc.init.ModBlocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.HashSet;

/**
 * Created by LordSaad.
 */
public class BlockCraneCore extends BlockModContainer implements ILadder {

	public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class);

	public BlockCraneCore() {
		super("crane_core", Material.GLASS);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getStateFromMeta(meta).withProperty(FACING, placer.getAdjustedHorizontalFacing().getOpposite());
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 7));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		PacketHandler.NETWORK.sendToAllAround(new PacketShowCraneParticles(pos, Color.GREEN), new NetworkRegistry.TargetPoint(worldIn.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 32));
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

				PacketHandler.NETWORK.sendToAllAround(new PacketShowCraneParticles(pos, Color.GREEN), new NetworkRegistry.TargetPoint(worldIn.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 32));
				return true;
			}

		} else {
			BlockPos seat = PosUtils.findCraneSeat(worldIn, pos);
			if (seat != null) {
				boolean seated = SittingUtil.seatPlayer(worldIn, seat, playerIn);
				if (seated)
					playerIn.openGui(CCMain.instance, 0, worldIn, seat.getX(), seat.getY(), seat.getZ());
				return seated;
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
			PacketHandler.NETWORK.sendToAllAround(new PacketShowCraneParticles(pos, Color.GREEN), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 32));
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

	@Override
	public TileEntity createTileEntity(World world, IBlockState iBlockState) {
		return new TileCraneCore();
	}

	@SideOnly(Side.CLIENT)
	public void initModel() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileCraneCore.class, new RenderCraneCore());
	}
}
