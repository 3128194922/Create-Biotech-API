package com.nobodiiiii.createbiotech.content.slimeclutch;

import com.nobodiiiii.createbiotech.registry.CBBlockEntityTypes;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.AbstractEncasedShaftBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.ticks.TickPriority;

public class SlimeClutchBlock extends AbstractEncasedShaftBlock implements IBE<SlimeClutchBlockEntity> {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public SlimeClutchBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWERED);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown())
			return super.getStateForPlacement(context).setValue(POWERED, false);
		Direction.Axis preferredAxis = getPreferredAxis(context);
		return defaultBlockState()
			.setValue(AXIS, preferredAxis == null ? context.getNearestLookingDirection().getAxis() : preferredAxis)
			.setValue(POWERED, false);
	}

	public void detachKinetics(Level worldIn, BlockPos pos, boolean reAttachNextTick) {
		BlockEntity be = worldIn.getBlockEntity(pos);
		if (!(be instanceof KineticBlockEntity kte))
			return;
		RotationPropagator.handleRemoved(worldIn, pos, kte);

		if (reAttachNextTick)
			worldIn.scheduleTick(pos, this, 1, TickPriority.EXTREMELY_HIGH);
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		BlockEntity be = worldIn.getBlockEntity(pos);
		if (!(be instanceof KineticBlockEntity kte))
			return;
		RotationPropagator.handleAdded(worldIn, pos, kte);
	}

	@Override
	public Class<SlimeClutchBlockEntity> getBlockEntityClass() {
		return SlimeClutchBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends SlimeClutchBlockEntity> getBlockEntityType() {
		return CBBlockEntityTypes.SLIME_CLUTCH.get();
	}
}
