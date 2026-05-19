package com.nobodiiiii.createbiotech.content.experience;

import com.nobodiiiii.createbiotech.registry.CBBlockEntityTypes;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BuddingExperienceBlock extends Block implements IBE<BuddingExperienceBlockEntity> {

	public BuddingExperienceBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		withBlockEntityDo(level, pos, be -> be.naturalTick(level, pos, random));
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		IBE.onRemove(state, level, pos, newState);
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public Class<BuddingExperienceBlockEntity> getBlockEntityClass() {
		return BuddingExperienceBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends BuddingExperienceBlockEntity> getBlockEntityType() {
		return CBBlockEntityTypes.BUDDING_EXPERIENCE.get();
	}
}
