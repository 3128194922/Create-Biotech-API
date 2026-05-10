package com.nobodiiiii.createbiotech.content.creeperblastchamber;

import com.simibubi.create.content.decoration.encasing.CasingBlock;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public class ExplosionProofCasingBlock extends CasingBlock {

	public ExplosionProofCasingBlock(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return CreeperBlastChamberBlockEntity.onStructureCasingWrenched(context.getLevel(), context.getClickedPos(),
			context.getPlayer());
	}
}
