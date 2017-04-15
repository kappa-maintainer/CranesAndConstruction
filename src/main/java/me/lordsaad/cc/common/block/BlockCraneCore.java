package me.lordsaad.cc.common.block;

import com.teamwizardry.librarianlib.common.base.block.BlockModContainer;
import com.teamwizardry.librarianlib.common.network.PacketHandler;
import me.lordsaad.cc.CCMain;
import me.lordsaad.cc.api.PosUtils;
import me.lordsaad.cc.api.SittingUtil;
import me.lordsaad.cc.client.render.RenderCraneCore;
import me.lordsaad.cc.common.network.PacketOpenGui;
import me.lordsaad.cc.common.tile.TileCraneCore;
import me.lordsaad.cc.init.ModBlocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.HashSet;

/**
 * Created by LordSaad.
 */
public class BlockCraneCore extends BlockModContainer {

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

	@Override
	public TileEntity createTileEntity(World world, IBlockState iBlockState) {
		return new TileCraneCore();
	}

	@SideOnly(Side.CLIENT)
	public void initModel() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileCraneCore.class, new RenderCraneCore());
	}
}
