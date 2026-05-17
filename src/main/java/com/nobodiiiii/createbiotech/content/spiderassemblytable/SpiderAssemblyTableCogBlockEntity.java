package com.nobodiiiii.createbiotech.content.spiderassemblytable;

import com.nobodiiiii.createbiotech.registry.CBBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SpiderAssemblyTableCogBlockEntity extends KineticBlockEntity {

	public SpiderAssemblyTableCogBlockEntity(BlockPos pos, BlockState state) {
		super(CBBlockEntityTypes.SPIDER_ASSEMBLY_TABLE_COG.get(), pos, state);
	}
}
