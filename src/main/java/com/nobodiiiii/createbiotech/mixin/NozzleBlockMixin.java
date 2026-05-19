package com.nobodiiiii.createbiotech.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.nobodiiiii.createbiotech.registry.CBBlocks;
import com.simibubi.create.content.kinetics.fan.NozzleBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

@Mixin(NozzleBlock.class)
public abstract class NozzleBlockMixin {

	@Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
	private void createBiotech$allowExperiencePumpEnds(BlockState state, LevelReader level, BlockPos pos,
		CallbackInfoReturnable<Boolean> cir) {
		Direction nozzleFacing = state.getValue(NozzleBlock.FACING);
		// Nozzle on our pump has FACING pointing TOWARD the pump (flipped from vanilla).
		BlockPos pumpPos = pos.relative(nozzleFacing);
		BlockState pumpState = level.getBlockState(pumpPos);
		if (!pumpState.is(CBBlocks.EXPERIENCE_PUMP.get())) {
			// Also accept the vanilla orientation for backwards-compat / placement during getStateForPlacement
			pumpPos = pos.relative(nozzleFacing.getOpposite());
			pumpState = level.getBlockState(pumpPos);
			if (!pumpState.is(CBBlocks.EXPERIENCE_PUMP.get()))
				return;
		}
		if (pumpState.getValue(com.nobodiiiii.createbiotech.content.experience.ExperiencePumpBlock.FACING)
			.getAxis() != nozzleFacing.getAxis())
			return;
		cir.setReturnValue(true);
	}

	@Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
	private void createBiotech$flipNozzleAgainstPump(BlockPlaceContext context,
		CallbackInfoReturnable<BlockState> cir) {
		BlockState placed = cir.getReturnValue();
		if (placed == null || !placed.hasProperty(BlockStateProperties.FACING))
			return;
		Direction clickedFace = context.getClickedFace();
		BlockPos pumpPos = context.getClickedPos()
			.relative(clickedFace.getOpposite());
		BlockState pumpState = context.getLevel()
			.getBlockState(pumpPos);
		if (!pumpState.is(CBBlocks.EXPERIENCE_PUMP.get()))
			return;
		// Flip FACING so the nozzle's "narrow end" points outward (visually inverse of fan nozzle).
		cir.setReturnValue(placed.setValue(BlockStateProperties.FACING, clickedFace.getOpposite()));
	}
}
