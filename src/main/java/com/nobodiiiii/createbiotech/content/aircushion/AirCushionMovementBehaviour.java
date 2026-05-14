package com.nobodiiiii.createbiotech.content.aircushion;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.gantry.GantryContraptionEntity;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class AirCushionMovementBehaviour implements MovementBehaviour {

	// The cushion only protrudes half a block, so its visible impact face sits on the block center plane.
	private static final double IMPACT_SAMPLE_DISTANCE = 1.0E-4d;
	private static final double MOVEMENT_EPSILON = 1.0E-4d;
	private static final double ESCAPE_PUSH_SPEED = 0.05d;

	@Override
	public void tick(MovementContext context) {
		if (context.world == null || context.world.isClientSide)
			return;
		if (context.position == null)
			return;

		AbstractContraptionEntity entity = context.contraption == null ? null : context.contraption.entity;
		if (entity instanceof ControlledContraptionEntity)
			return;
		if (entity instanceof GantryContraptionEntity)
			return;

		Vec3 collisionNormal = getCollisionNormal(context);
		boolean collision = collisionNormal != null && collidesWithWorld(context.position, context, collisionNormal);
		context.stall = false;

		if (collision && entity != null) {
			boolean escapeFromBlock = shouldApplyEscapePush(context, collisionNormal);
			applyCollisionResponse(context, entity, collisionNormal, escapeFromBlock);
		}
	}

	private static boolean collidesWithWorld(Vec3 position, MovementContext context, Vec3 collisionNormal) {
		BlockPos blockPos = BlockPos.containing(position.add(collisionNormal.scale(IMPACT_SAMPLE_DISTANCE)));
		BlockState worldState = context.world.getBlockState(blockPos);
		if (worldState.canBeReplaced())
			return false;
		return !worldState.getCollisionShape(context.world, blockPos)
			.isEmpty();
	}

	private static Vec3 getCollisionNormal(MovementContext context) {
		if (!context.state.hasProperty(AirCushionBlock.FACING))
			return null;

		Direction collisionFace = context.state.getValue(AirCushionBlock.FACING)
			.getOpposite();
		Vec3 collisionNormal = context.rotation.apply(Vec3.atLowerCornerOf(collisionFace.getNormal()))
			.normalize();
		if (collisionNormal.lengthSqr() < MOVEMENT_EPSILON)
			return null;
		return collisionNormal;
	}

	private static void applyCollisionResponse(MovementContext context, AbstractContraptionEntity entity,
		Vec3 collisionNormal, boolean escapeFromBlock) {
		clipEntityPosition(entity, collisionNormal);
		clipEntityMotion(entity, collisionNormal, escapeFromBlock);

		if (entity instanceof CarriageContraptionEntity carriageEntity && carriageEntity.getCarriage() != null)
			clipTrainSpeed(context.motion, carriageEntity.getCarriage().train, collisionNormal, escapeFromBlock);

		if (entity instanceof OrientedContraptionEntity orientedEntity)
			clipCoupledCarts(orientedEntity, collisionNormal, escapeFromBlock);

		for (Entity current = entity.getVehicle(); current != null; current = current.getVehicle()) {
			clipEntityPosition(current, collisionNormal);
			clipEntityMotion(current, collisionNormal, escapeFromBlock);
		}
	}

	private static boolean shouldApplyEscapePush(MovementContext context, Vec3 collisionNormal) {
		double inwardMotion = context.motion.dot(collisionNormal);
		if (inwardMotion <= MOVEMENT_EPSILON)
			return true;

		Vec3 previousPosition = context.position.subtract(context.motion);
		return collidesWithWorld(previousPosition, context, collisionNormal);
	}

	private static void clipCoupledCarts(OrientedContraptionEntity entity, Vec3 collisionNormal,
		boolean escapeFromBlock) {
		var coupledCarts = entity.getCoupledCartsIfPresent();
		if (coupledCarts == null)
			return;

		clipEntityPosition(coupledCarts.getFirst()
			.cart(), collisionNormal);
		clipEntityMotion(coupledCarts.getFirst()
			.cart(), collisionNormal, escapeFromBlock);
		clipEntityPosition(coupledCarts.getSecond()
			.cart(), collisionNormal);
		clipEntityMotion(coupledCarts.getSecond()
			.cart(), collisionNormal, escapeFromBlock);
	}

	private static void clipTrainSpeed(Vec3 actorMotion, Train train, Vec3 collisionNormal, boolean escapeFromBlock) {
		Vec3 adjustedMotion = adjustMotionAgainstCollision(actorMotion, collisionNormal, escapeFromBlock);
		if (adjustedMotion.equals(actorMotion))
			return;

		double referenceSpeed = getTrainReferenceSpeed(train);
		if (Math.abs(referenceSpeed) < MOVEMENT_EPSILON && actorMotion.lengthSqr() < MOVEMENT_EPSILON)
			return;

		double adjustedSpeed = getAdjustedTrainSpeed(actorMotion, adjustedMotion, referenceSpeed, escapeFromBlock);
		train.speed = adjustedSpeed;
		if (train.speedBeforeStall != null)
			train.speedBeforeStall = adjustedSpeed;
		if (escapeFromBlock)
			train.targetSpeed = adjustedSpeed;
	}

	private static void clipEntityPosition(Entity entity, Vec3 collisionNormal) {
		if (entity == null)
			return;

		Vec3 displacement = entity.position()
			.subtract(entity.xo, entity.yo, entity.zo);
		double intoCollisionFace = displacement.dot(collisionNormal);
		if (intoCollisionFace <= MOVEMENT_EPSILON)
			return;

		Vec3 correctedPosition = entity.position()
			.subtract(collisionNormal.scale(intoCollisionFace));
		entity.setPos(correctedPosition.x, correctedPosition.y, correctedPosition.z);
		entity.hurtMarked = true;
	}

	private static void clipEntityMotion(Entity entity, Vec3 collisionNormal, boolean escapeFromBlock) {
		if (entity == null)
			return;

		Vec3 motion = entity.getDeltaMovement();
		Vec3 adjustedMotion = adjustMotionAgainstCollision(motion, collisionNormal, escapeFromBlock);
		if (adjustedMotion.equals(motion))
			return;

		if (entity instanceof AbstractContraptionEntity contraptionEntity)
			contraptionEntity.setContraptionMotion(adjustedMotion);
		else
			entity.setDeltaMovement(adjustedMotion);
		entity.hurtMarked = true;

		if (entity instanceof MinecartFurnace furnaceCart)
			clipFurnacePush(furnaceCart, collisionNormal, escapeFromBlock);
	}

	private static void clipFurnacePush(MinecartFurnace furnaceCart, Vec3 collisionNormal, boolean escapeFromBlock) {
		Vec3 horizontalNormal = new Vec3(collisionNormal.x, 0, collisionNormal.z);
		if (horizontalNormal.lengthSqr() < MOVEMENT_EPSILON)
			return;

		horizontalNormal = horizontalNormal.normalize();

		CompoundTag nbt = furnaceCart.serializeNBT();
		Vec3 push = new Vec3(nbt.getDouble("PushX"), 0, nbt.getDouble("PushZ"));
		Vec3 adjustedPush = adjustMotionAgainstCollision(push, horizontalNormal, escapeFromBlock);
		if (adjustedPush.equals(push))
			return;

		nbt.putDouble("PushX", adjustedPush.x);
		nbt.putDouble("PushZ", adjustedPush.z);
		furnaceCart.deserializeNBT(nbt);
	}

	private static double getTrainReferenceSpeed(Train train) {
		double currentSpeed = train.speedBeforeStall != null ? train.speedBeforeStall : train.speed;
		if (Math.abs(currentSpeed) >= MOVEMENT_EPSILON)
			return currentSpeed;
		if (Math.abs(train.targetSpeed) >= MOVEMENT_EPSILON)
			return train.targetSpeed;
		return 0;
	}

	private static double getAdjustedTrainSpeed(Vec3 actorMotion, Vec3 adjustedMotion, double referenceSpeed,
		boolean escapeFromBlock) {
		if (actorMotion.lengthSqr() < MOVEMENT_EPSILON) {
			if (!escapeFromBlock || Math.abs(referenceSpeed) < MOVEMENT_EPSILON)
				return referenceSpeed;
			return -Math.copySign(ESCAPE_PUSH_SPEED, referenceSpeed);
		}

		Vec3 travelAxis = actorMotion.normalize();
		double alongTravel = adjustedMotion.dot(travelAxis);
		if (escapeFromBlock && alongTravel > -ESCAPE_PUSH_SPEED)
			alongTravel = -ESCAPE_PUSH_SPEED;

		double speedMagnitude = Math.abs(alongTravel);
		if (speedMagnitude < MOVEMENT_EPSILON)
			return 0;

		double speed = Math.copySign(speedMagnitude, referenceSpeed);
		if (alongTravel < 0)
			speed *= -1;
		return speed;
	}

	private static Vec3 adjustMotionAgainstCollision(Vec3 motion, Vec3 collisionNormal, boolean escapeFromBlock) {
		Vec3 adjustedMotion = removeVelocityIntoCollisionFace(motion, collisionNormal);
		if (!escapeFromBlock)
			return adjustedMotion;
		return ensureEscapeVelocity(adjustedMotion, collisionNormal);
	}

	private static Vec3 ensureEscapeVelocity(Vec3 motion, Vec3 collisionNormal) {
		double normalSpeed = motion.dot(collisionNormal);
		if (normalSpeed <= -ESCAPE_PUSH_SPEED)
			return motion;
		return motion.subtract(collisionNormal.scale(normalSpeed + ESCAPE_PUSH_SPEED));
	}

	private static Vec3 removeVelocityIntoCollisionFace(Vec3 motion, Vec3 collisionNormal) {
		double intoCollisionFace = motion.dot(collisionNormal);
		if (intoCollisionFace <= MOVEMENT_EPSILON)
			return motion;
		return motion.subtract(collisionNormal.scale(intoCollisionFace));
	}
}
