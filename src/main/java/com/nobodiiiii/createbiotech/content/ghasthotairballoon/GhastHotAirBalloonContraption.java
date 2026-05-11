package com.nobodiiiii.createbiotech.content.ghasthotairballoon;

import com.nobodiiiii.createbiotech.registry.CBContraptionTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.contraptions.TranslatingContraption;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;

public class GhastHotAirBalloonContraption extends TranslatingContraption {

	public static final int PLATFORM_RADIUS = 2;

	@Override
	public ContraptionType getType() {
		return CBContraptionTypes.GHAST_HOT_AIR_BALLOON.value();
	}

	@Override
	public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
		anchor = pos;
		bounds = new AABB(BlockPos.ZERO);
		BlockState casing = AllBlocks.ANDESITE_CASING.getDefaultState();
		for (int dx = -PLATFORM_RADIUS; dx <= PLATFORM_RADIUS; dx++) {
			for (int dz = -PLATFORM_RADIUS; dz <= PLATFORM_RADIUS; dz++) {
				BlockPos local = new BlockPos(dx, 0, dz);
				StructureBlockInfo info = new StructureBlockInfo(local, casing, null);
				blocks.put(local, info);
				bounds = bounds.minmax(new AABB(local));
			}
		}
		return !blocks.isEmpty();
	}

	@Override
	public boolean canBeStabilized(Direction facing, BlockPos localPos) {
		return false;
	}

	@Override
	public void addBlocksToWorld(Level world, StructureTransform transform) {
		disassembled = true;
	}

	@Override
	public void removeBlocksFromWorld(Level world, BlockPos offset) {
	}
}
