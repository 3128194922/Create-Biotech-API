package com.nobodiiiii.createbiotech.content.ghasthotairballoon;

import com.nobodiiiii.createbiotech.CreateBiotech;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.AssemblyException;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateBiotech.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GhastHotAirBalloonHandler {

	private GhastHotAirBalloonHandler() {}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onWrenchUseOnGhast(PlayerInteractEvent.EntityInteract event) {
		Entity target = event.getTarget();
		if (!(target instanceof Ghast ghast))
			return;

		Player player = event.getEntity();
		if (player == null)
			return;

		InteractionHand hand = event.getHand();
		ItemStack held = player.getItemInHand(hand);
		if (!AllItems.WRENCH.isIn(held))
			return;

		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);

		Level level = event.getLevel();
		if (level.isClientSide)
			return;

		if (hasBalloonPassenger(ghast))
			return;

		GhastHotAirBalloonContraption contraption = new GhastHotAirBalloonContraption();
		BlockPos anchor = ghast.blockPosition().below((int) GhastHotAirBalloonEntity.Y_OFFSET);
		try {
			if (!contraption.assemble(level, anchor))
				return;
		} catch (AssemblyException e) {
			return;
		}

		GhastHotAirBalloonEntity entity = GhastHotAirBalloonEntity.create(level, contraption, ghast);
		entity.setPos(ghast.getX(), ghast.getY() - GhastHotAirBalloonEntity.Y_OFFSET, ghast.getZ());
		level.addFreshEntity(entity);
		entity.startRiding(ghast, true);
	}

	private static boolean hasBalloonPassenger(Ghast ghast) {
		for (Entity passenger : ghast.getPassengers()) {
			if (passenger instanceof GhastHotAirBalloonEntity)
				return true;
		}
		return false;
	}
}
