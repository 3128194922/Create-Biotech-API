package com.nobodiiiii.createbiotech;

import java.util.List;
import java.util.UUID;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.mounted.MinecartContraptionItem;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(CreateBiotech.MOD_ID)
public class CreateBiotech {
	public static final String MOD_ID = "create_biotech";

	public CreateBiotech() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	@SubscribeEvent
	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		processInteraction(event.getEntity(), event.getTarget(), event.getHand(), event);
	}

	@SubscribeEvent
	public void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
		processInteraction(event.getEntity(), event.getTarget(), event.getHand(), event);
	}

	private void processInteraction(Player player, Entity target, InteractionHand hand,
									PlayerInteractEvent event) {
		if (player == null || target == null)
			return;
		ItemStack held = player.getItemInHand(hand);

		if (target instanceof LivingEntity livingTarget && !(target instanceof Player)) {
			if (held.getItem() instanceof MinecartContraptionItem) {
				handleAttachContraption(event, player, livingTarget, held);
				return;
			}
		}

		if (AllItems.WRENCH.isIn(held)) {
			handleWrenchPickup(event, player, target);
		}
	}

	private void handleAttachContraption(PlayerInteractEvent event, Player player,
										 LivingEntity creature, ItemStack held) {
		Level level = event.getLevel();
		if (level.isClientSide)
			return;

		if (!player.isCreative() && !isOwnedBy(creature, player))
			return;

		CompoundTag tag = held.getOrCreateTag();
		if (!tag.contains("Contraption"))
			return;

		for (Entity passenger : creature.getPassengers()) {
			if (passenger instanceof AbstractContraptionEntity)
				return;
		}

		CompoundTag contraptionTag = tag.getCompound("Contraption");
		Contraption contraption = Contraption.fromNBT(level, contraptionTag, false);

		if (contraption == null)
			return;

		contraption.stalled = false;
		contraption.disassembled = false;

		Direction initialOrientation = readDirection(contraptionTag, "InitialOrientation");
		OrientedContraptionEntity contraptionEntity;

		try {
			contraptionEntity = OrientedContraptionEntity.createAtYaw(level, contraption,
				initialOrientation, creature.getYRot());
		} catch (NullPointerException e) {
			contraptionEntity = new OrientedContraptionEntity(AllEntityTypes.ORIENTED_CONTRAPTION.get(), level);
			setContraptionViaReflection(contraptionEntity, contraption);
			contraptionEntity.targetYaw = creature.getYRot();
			contraptionEntity.yaw = creature.getYRot();
			contraptionEntity.prevYaw = creature.getYRot();
		}

		contraptionEntity.setPos(creature.position());
		contraptionEntity.startRiding(creature);
		level.addFreshEntity(contraptionEntity);

		if (!player.isCreative())
			held.shrink(1);

		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);
	}

	private boolean isOwnedBy(LivingEntity creature, Player player) {
		UUID playerId = player.getUUID();
		if (creature instanceof TamableAnimal tamable) {
			return tamable.isTame() && playerId.equals(tamable.getOwnerUUID());
		}
		if (creature instanceof AbstractHorse horse) {
			UUID owner = horse.getOwnerUUID();
			return owner != null && owner.equals(playerId);
		}
		return false;
	}

	private void setContraptionViaReflection(OrientedContraptionEntity entity, Contraption contraption) {
		try {
			java.lang.reflect.Method method = AbstractContraptionEntity.class
				.getDeclaredMethod("setContraption", Contraption.class);
			method.setAccessible(true);
			method.invoke(entity, contraption);
		} catch (Exception ignored) {
		}
	}

	private void handleWrenchPickup(PlayerInteractEvent event, Player player, Entity target) {
		Entity checkEntity = target;
		if (checkEntity instanceof AbstractContraptionEntity)
			checkEntity = checkEntity.getVehicle();
		if (checkEntity instanceof AbstractContraptionEntity)
			return;

		if (!(checkEntity instanceof LivingEntity creature))
			return;
		if (!creature.isAlive())
			return;

		List<Entity> passengers = creature.getPassengers();
		OrientedContraptionEntity oce = null;
		for (Entity passenger : passengers) {
			if (passenger instanceof OrientedContraptionEntity) {
				oce = (OrientedContraptionEntity) passenger;
				break;
			}
		}
		if (oce == null)
			return;

		if (event.getLevel().isClientSide)
			return;

		Contraption contraption = oce.getContraption();
		contraption.stop(event.getLevel());

		CompoundTag contraptionTag = contraption.writeNBT(false);
		contraptionTag.remove("UUID");
		contraptionTag.remove("Pos");
		contraptionTag.remove("Motion");

		ItemStack generatedStack = AllItems.MINECART_CONTRAPTION.asStack();
		generatedStack.getOrCreateTag().put("Contraption", contraptionTag);
		generatedStack.setHoverName(creature.getCustomName());

		player.getInventory().placeItemBackInInventory(generatedStack);
		oce.discard();

		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);
	}

	private static Direction readDirection(CompoundTag tag, String key) {
		if (!tag.contains(key))
			return Direction.NORTH;
		String name = tag.getString(key);
		try {
			return Direction.byName(name);
		} catch (IllegalArgumentException e) {
			return Direction.NORTH;
		}
	}
}
