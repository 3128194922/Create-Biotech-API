package com.nobodiiiii.createbiotech.content.shulkerpackager;

import com.nobodiiiii.createbiotech.network.CBPackets;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ShulkerPackagerItem extends BlockItem {

	public ShulkerPackagerItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		if (ShulkerPackagerArmInteractions.isSelectable(world.getBlockState(pos)))
			return InteractionResult.SUCCESS;
		return super.useOn(ctx);
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, Level world, Player player, ItemStack stack,
		BlockState state) {
		if (!world.isClientSide && player instanceof ServerPlayer sp)
			CBPackets.sendToPlayer(new ShulkerPackagerPlacementPacket.ClientBoundRequest(pos), sp);
		return super.updateCustomBlockEntityTag(pos, world, player, stack, state);
	}

	@Override
	public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player player) {
		return !ShulkerPackagerArmInteractions.isSelectable(state);
	}
}
