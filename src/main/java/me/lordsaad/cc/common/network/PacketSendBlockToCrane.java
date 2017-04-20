package me.lordsaad.cc.common.network;

import com.teamwizardry.librarianlib.features.network.PacketBase;
import com.teamwizardry.librarianlib.features.saving.Save;
import com.teamwizardry.librarianlib.features.saving.SaveMethodGetter;
import com.teamwizardry.librarianlib.features.saving.SaveMethodSetter;
import kotlin.Pair;
import me.lordsaad.cc.common.tile.TileCraneCore;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by LordSaad.
 */
public class PacketSendBlockToCrane extends PacketBase {

	@Save
	private BlockPos pos;
	private Pair<IBlockState, BlockPos> pair;

	public PacketSendBlockToCrane() {
	}

	public PacketSendBlockToCrane(BlockPos pos, Pair<IBlockState, BlockPos> pair) {
		this.pos = pos;
		this.pair = pair;
	}

	@SaveMethodGetter(saveName = "pair")
	public NBTTagCompound nextPairGetter() {
		NBTTagCompound nbt = new NBTTagCompound();
		if (pair == null) return nbt;

		NBTUtil.writeBlockState(nbt, pair.getFirst()).setTag("block_pos", NBTUtil.createPosTag(pair.getSecond()));
		return nbt;
	}

	@SaveMethodSetter(saveName = "pair")
	public void nextPairSetter(NBTTagCompound nbt) {
		pair = new Pair<>(NBTUtil.readBlockState(nbt), NBTUtil.getPosFromTag(nbt.getCompoundTag("block_pos")));
	}

	@Override
	public void handle(MessageContext messageContext) {
		World world = messageContext.getServerHandler().playerEntity.world;
		TileCraneCore tile = (TileCraneCore) world.getTileEntity(pos);

		if (tile == null) return;
		if (world.isRemote) return;

		tile.queue.add(pair);
		tile.markDirty();
	}
}
