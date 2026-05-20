package com.nobodiiiii.createbiotech.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.fluids.tank.FluidTankCTBehaviour;
import com.simibubi.create.foundation.block.connected.CTModel;

import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelData.Builder;
import net.minecraftforge.client.model.data.ModelProperty;

public class ExperienceTankModel extends CTModel {

	private static final ModelProperty<CullData> CULL_PROPERTY = new ModelProperty<>();

	public static ExperienceTankModel create(BakedModel originalModel) {
		return new ExperienceTankModel(originalModel);
	}

	private ExperienceTankModel(BakedModel originalModel) {
		super(originalModel, new FluidTankCTBehaviour(CBSpriteShifts.EXPERIENCE_TANK,
			CBSpriteShifts.EXPERIENCE_TANK_TOP, CBSpriteShifts.EXPERIENCE_TANK_INNER));
	}

	@Override
	protected Builder gatherModelData(Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state,
		ModelData blockEntityData) {
		super.gatherModelData(builder, world, pos, state, blockEntityData);
		CullData cullData = new CullData();
		for (Direction direction : Iterate.horizontalDirections)
			cullData.setCulled(direction, ConnectivityHandler.isConnected(world, pos, pos.relative(direction)));
		return builder.with(CULL_PROPERTY, cullData);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData,
		RenderType renderType) {
		if (side != null)
			return Collections.emptyList();

		List<BakedQuad> quads = new ArrayList<>();
		for (Direction direction : Iterate.directions) {
			if (extraData.has(CULL_PROPERTY) && extraData.get(CULL_PROPERTY).isCulled(direction))
				continue;
			quads.addAll(super.getQuads(state, direction, rand, extraData, renderType));
		}
		quads.addAll(super.getQuads(state, null, rand, extraData, renderType));
		return quads;
	}

	private static class CullData {
		private final boolean[] culledFaces = new boolean[4];

		private CullData() {
			Arrays.fill(culledFaces, false);
		}

		private void setCulled(Direction face, boolean culled) {
			if (face.getAxis().isVertical())
				return;
			culledFaces[face.get2DDataValue()] = culled;
		}

		private boolean isCulled(Direction face) {
			if (face.getAxis().isVertical())
				return false;
			return culledFaces[face.get2DDataValue()];
		}
	}
}
