package me.lordsaad.cc.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Created by LordSaad.
 */
public class EntitySeat extends Entity {

	public EntitySeat(World world) {
		super(world);
		this.noClip = true;
		this.height = 0.01F;
		this.width = 0.01F;
	}

	@Override
	public double getMountedYOffset() {
		return 0;
	}

	@Override
	protected boolean shouldSetPosAfterLoading() {
		return false;
	}

	@Override
	public void onUpdate() {
		for (Entity entity : getPassengers()) {
			if (entity.isSneaking()) {
				world.removeEntity(this);
			}
		}
		if (getPassengers().isEmpty() || world.isAirBlock(getPosition())) {
			world.removeEntity(this);
		}
	}

	@Override
	protected void entityInit() {
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
	}

}
