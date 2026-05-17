package com.nobodiiiii.createbiotech.content.slimeclutch;

import com.nobodiiiii.createbiotech.registry.CBBlockEntityTypes;
import com.simibubi.create.content.kinetics.transmission.SplitShaftBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SlimeClutchBlockEntity extends SplitShaftBlockEntity {

	private boolean pendingStateUpdate;
	private float stressAtTrip;

	public SlimeClutchBlockEntity(BlockPos pos, BlockState state) {
		super(CBBlockEntityTypes.SLIME_CLUTCH.get(), pos, state);
	}

	@Override
	public float getRotationSpeedModifier(Direction face) {
		if (hasSource()) {
			if (face != getSourceFacing() && getBlockState().getValue(BlockStateProperties.POWERED))
				return 0;
		}
		return 1;
	}

	@Override
	public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
		super.updateFromNetwork(maxStress, currentStress, networkSize);
		if (level == null || level.isClientSide)
			return;

		boolean powered = getBlockState().getValue(BlockStateProperties.POWERED);
		if (powered) {
			if (maxStress > stressAtTrip)
				pendingStateUpdate = true;
		} else if (isOverStressed()) {
			stressAtTrip = currentStress;
			pendingStateUpdate = true;
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide || !pendingStateUpdate)
			return;
		pendingStateUpdate = false;

		BlockState state = getBlockState();
		boolean powered = state.getValue(BlockStateProperties.POWERED);
		boolean shouldBePowered;
		if (powered)
			shouldBePowered = stressAtTrip >= getMaxStress();
		else
			shouldBePowered = isOverStressed();

		if (powered == shouldBePowered)
			return;

		if (state.getBlock() instanceof SlimeClutchBlock clutch)
			clutch.detachKinetics(level, getBlockPos(), true);
		level.setBlock(getBlockPos(), state.setValue(BlockStateProperties.POWERED, shouldBePowered), 2 | 16);
	}

	private float getMaxStress() {
		return capacity;
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putFloat("StressAtTrip", stressAtTrip);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		stressAtTrip = compound.getFloat("StressAtTrip");
	}
}
