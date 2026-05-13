package com.nobodiiiii.createbiotech.content.ghasthotairballoon;

import java.util.Collection;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsHandler;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsRenderer;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GhastHelmMovementBehaviour implements MovementBehaviour {

	static class LeverAngles {
		LerpedFloat steering = LerpedFloat.linear();
		LerpedFloat speed = LerpedFloat.linear();
		LerpedFloat equipAnimation = LerpedFloat.linear();
	}

	@Override
	public ItemStack canBeDisabledVia(MovementContext context) {
		return null;
	}

	@Override
	public void stopMoving(MovementContext context) {
		context.contraption.entity.stopControlling(context.localPos);
		MovementBehaviour.super.stopMoving(context);
	}

	@Override
	public void tick(MovementContext context) {
		MovementBehaviour.super.tick(context);
		if (!context.world.isClientSide)
			return;
		getAngles(context).steering.tickChaser();
		getAngles(context).speed.tickChaser();
		getAngles(context).equipAnimation.tickChaser();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffer) {
		if (VisualizationManager.supportsVisualization(context.world))
			return;
		if (!(context.contraption.entity instanceof GhastHotAirBalloonEntity balloon))
			return;
		LeverAngles angles = getAngles(context);
		updateTargetAngles(context, balloon, angles);

		float pt = AnimationTickHolder.getPartialTicks();
		ControlsRenderer.render(context, renderWorld, matrices, buffer, angles.equipAnimation.getValue(pt),
			angles.speed.getValue(pt), angles.steering.getValue(pt));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ActorVisual createVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld,
		MovementContext movementContext) {
		return new GhastHelmActorVisual(visualizationContext, simulationWorld, movementContext);
	}

	static LeverAngles getAngles(MovementContext context) {
		if (!(context.temporaryData instanceof LeverAngles))
			context.temporaryData = new LeverAngles();
		return (LeverAngles) context.temporaryData;
	}

	static void updateTargetAngles(MovementContext context, GhastHotAirBalloonEntity balloon, LeverAngles angles) {
		AbstractContraptionEntity entity = context.contraption.entity;
		Direction controlForward = balloon.getInitialOrientation().getOpposite();
		boolean inverted = context.state.hasProperty(ControlsBlock.FACING)
			&& !context.state.getValue(ControlsBlock.FACING).equals(controlForward);

		if (ControlsHandler.getContraption() == entity && ControlsHandler.getControlsPos() != null
			&& ControlsHandler.getControlsPos().equals(context.localPos)) {
			Collection<Integer> pressed = ControlsHandler.currentlyPressed;
			angles.equipAnimation.chase(1, .2f, Chaser.EXP);
			angles.steering.chase((pressed.contains(3) ? 1 : 0) + (pressed.contains(2) ? -1 : 0), 0.2f, Chaser.EXP);
			float speedInput = (pressed.contains(0) ? 1 : 0) + (pressed.contains(1) ? -1 : 0);
			angles.speed.chase(inverted ? -speedInput : speedInput, 0.2f, Chaser.EXP);
			return;
		}

		angles.equipAnimation.chase(0, .2f, Chaser.EXP);
		angles.steering.chase(0, 0, Chaser.EXP);
		angles.speed.chase(0, 0, Chaser.EXP);
	}
}
