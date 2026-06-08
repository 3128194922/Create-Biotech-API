package com.nobodiiiii.createbiotech.content.shulkerpackager;

import java.util.ArrayList;
import java.util.List;

import com.nobodiiiii.createbiotech.registry.CBBlockEntityTypes;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint.Mode;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;

public class ShulkerPackagerBlockEntity extends PackagerBlockEntity {

	public static final int TRANSFER_DELAY = 16;

	List<ArmInteractionPoint> inputs;
	List<ArmInteractionPoint> outputs;
	ListTag interactionPointTag;
	boolean updateInteractionPoints;
	boolean transferInProgress;
	int heldBoxIdleTicks;

	protected ScrollOptionBehaviour<ArmBlockEntity.SelectionMode> selectionMode;
	protected int lastInputIndex;
	protected int lastOutputIndex;

	public ShulkerPackagerBlockEntity(BlockPos pos, BlockState state) {
		super(CBBlockEntityTypes.SHULKER_PACKAGER.get(), pos, state);
		inventory = new ShulkerPackagerItemHandler(this);
		inputs = new ArrayList<>();
		outputs = new ArrayList<>();
		interactionPointTag = new ListTag();
		updateInteractionPoints = true;
		transferInProgress = false;
		heldBoxIdleTicks = 0;
		lastInputIndex = -1;
		lastOutputIndex = -1;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		selectionMode = new ScrollOptionBehaviour<>(ArmBlockEntity.SelectionMode.class,
			CreateLang.translateDirect("logistics.when_multiple_outputs_available"), this,
			new CenteredSideValueBoxTransform((state, side) -> !side.getAxis()
				.isVertical()));
		behaviours.add(selectionMode);
	}

	@Override
	public void tick() {
		initInteractionPoints();
		boolean wasAnimating = animationTicks > 0;
		super.tick();

		if (level == null || level.isClientSide)
			return;

		if (wasAnimating && animationTicks == 0 && transferInProgress)
			finishOutgoingTransfer();

		if (animationTicks != 0) {
			heldBoxIdleTicks = 0;
			return;
		}

		if (!heldBox.isEmpty()) {
			heldBoxIdleTicks++;
			if (heldBoxIdleTicks >= TRANSFER_DELAY)
				attemptTransferToOutput();
			return;
		}

		heldBoxIdleTicks = 0;
		attemptPullFromInput();
	}

	public boolean canAcceptTransferredPackage(ItemStack stack) {
		return !stack.isEmpty() && PackageItem.isPackage(stack) && !transferInProgress && animationTicks == 0
			&& heldBox.isEmpty() && queuedExitingPackages.isEmpty();
	}

	public boolean startIncomingTransfer(ItemStack stack) {
		if (!canAcceptTransferredPackage(stack))
			return false;
		heldBox = stack.copy();
		previouslyUnwrapped = ItemStack.EMPTY;
		animationInward = false;
		animationTicks = CYCLE;
		heldBoxIdleTicks = 0;
		notifyUpdate();
		setChanged();
		return true;
	}

	public boolean canExportHeldBoxTo(ShulkerPackagerBlockEntity target) {
		return target != null && target != this && !heldBox.isEmpty() && PackageItem.isPackage(heldBox)
			&& heldBoxIdleTicks >= TRANSFER_DELAY && animationTicks == 0 && !transferInProgress
			&& queuedExitingPackages.isEmpty() && target.canAcceptTransferredPackage(heldBox);
	}

	public List<ArmInteractionPoint> getInputs() {
		initInteractionPoints();
		return inputs;
	}

	public List<ArmInteractionPoint> getOutputs() {
		initInteractionPoints();
		return outputs;
	}

	public void setInteractionPointTag(ListTag interactionPointTag) {
		this.interactionPointTag = interactionPointTag;
		updateInteractionPoints = true;
		notifyUpdate();
		setChanged();
	}

	private void attemptPullFromInput() {
		if (inputs.isEmpty())
			return;

		boolean foundInput = false;
		int startIndex = selectionMode.get() == ArmBlockEntity.SelectionMode.PREFER_FIRST ? 0 : lastInputIndex + 1;
		int scanRange = selectionMode.get() == ArmBlockEntity.SelectionMode.FORCED_ROUND_ROBIN ? lastInputIndex + 2
			: inputs.size();
		if (scanRange > inputs.size())
			scanRange = inputs.size();

		for (int i = startIndex; i < scanRange; i++) {
			ShulkerPackagerBlockEntity source = getConnectedPackager(inputs.get(i));
			if (source == null || !source.canExportHeldBoxTo(this))
				continue;
			if (!source.startOutgoingTransfer(this))
				continue;
			lastInputIndex = i;
			foundInput = true;
			break;
		}

		if (!foundInput && selectionMode.get() == ArmBlockEntity.SelectionMode.ROUND_ROBIN)
			lastInputIndex = -1;
		if (lastInputIndex == inputs.size() - 1)
			lastInputIndex = -1;
	}

	private void attemptTransferToOutput() {
		if (outputs.isEmpty() || heldBox.isEmpty())
			return;

		boolean foundOutput = false;
		int startIndex = selectionMode.get() == ArmBlockEntity.SelectionMode.PREFER_FIRST ? 0 : lastOutputIndex + 1;
		int scanRange = selectionMode.get() == ArmBlockEntity.SelectionMode.FORCED_ROUND_ROBIN ? lastOutputIndex + 2
			: outputs.size();
		if (scanRange > outputs.size())
			scanRange = outputs.size();

		for (int i = startIndex; i < scanRange; i++) {
			ShulkerPackagerBlockEntity target = getConnectedPackager(outputs.get(i));
			if (target == null || target == this)
				continue;
			if (!startOutgoingTransfer(target))
				continue;
			lastOutputIndex = i;
			foundOutput = true;
			break;
		}

		if (!foundOutput && selectionMode.get() == ArmBlockEntity.SelectionMode.ROUND_ROBIN)
			lastOutputIndex = -1;
		if (lastOutputIndex == outputs.size() - 1)
			lastOutputIndex = -1;
	}

	private boolean startOutgoingTransfer(ShulkerPackagerBlockEntity target) {
		if (!canExportHeldBoxTo(target))
			return false;
		ItemStack box = heldBox.copy();
		if (!target.startIncomingTransfer(box))
			return false;
		transferInProgress = true;
		heldBox = ItemStack.EMPTY;
		previouslyUnwrapped = box;
		animationInward = true;
		animationTicks = CYCLE;
		heldBoxIdleTicks = 0;
		notifyUpdate();
		setChanged();
		return true;
	}

	private void finishOutgoingTransfer() {
		transferInProgress = false;
		previouslyUnwrapped = ItemStack.EMPTY;
		notifyUpdate();
		setChanged();
	}

	private ShulkerPackagerBlockEntity getConnectedPackager(ArmInteractionPoint point) {
		if (level == null || point == null || !point.isValid())
			return null;
		BlockEntity blockEntity = level.getBlockEntity(point.getPos());
		return blockEntity instanceof ShulkerPackagerBlockEntity packager ? packager : null;
	}

	private void initInteractionPoints() {
		if (!updateInteractionPoints || interactionPointTag == null || level == null)
			return;
		if (!isAreaActuallyLoaded(worldPosition, ArmBlockEntity.getRange() + 1))
			return;

		inputs.clear();
		outputs.clear();

		for (Tag tag : interactionPointTag) {
			ArmInteractionPoint point = ArmInteractionPoint.deserialize((CompoundTag) tag, level, worldPosition);
			if (point == null)
				continue;
			if (point.getMode() == Mode.DEPOSIT)
				outputs.add(point);
			else if (point.getMode() == Mode.TAKE)
				inputs.add(point);
		}

		updateInteractionPoints = false;
	}

	private boolean isAreaActuallyLoaded(BlockPos center, int range) {
		if (!level.isAreaLoaded(center, range))
			return false;
		if (!level.isClientSide)
			return true;

		int minY = center.getY() - range;
		int maxY = center.getY() + range;
		if (maxY < level.getMinBuildHeight() || minY >= level.getMaxBuildHeight())
			return false;

		int minX = center.getX() - range;
		int minZ = center.getZ() - range;
		int maxX = center.getX() + range;
		int maxZ = center.getZ() + range;
		int minChunkX = SectionPos.blockToSectionCoord(minX);
		int maxChunkX = SectionPos.blockToSectionCoord(maxX);
		int minChunkZ = SectionPos.blockToSectionCoord(minZ);
		int maxChunkZ = SectionPos.blockToSectionCoord(maxZ);

		ChunkSource chunkSource = level.getChunkSource();
		for (int chunkX = minChunkX; chunkX <= maxChunkX; ++chunkX)
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ)
				if (!chunkSource.hasChunk(chunkX, chunkZ))
					return false;

		return true;
	}

	private void writeInteractionPoints(CompoundTag compound) {
		if (updateInteractionPoints && interactionPointTag != null) {
			compound.put("InteractionPoints", interactionPointTag);
			return;
		}

		ListTag pointsNBT = new ListTag();
		inputs.stream()
			.map(aip -> aip.serialize(worldPosition))
			.forEach(pointsNBT::add);
		outputs.stream()
			.map(aip -> aip.serialize(worldPosition))
			.forEach(pointsNBT::add);
		compound.put("InteractionPoints", pointsNBT);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		writeInteractionPoints(compound);
		compound.putBoolean("TransferInProgress", transferInProgress);
	}

	@Override
	public void writeSafe(CompoundTag compound) {
		super.writeSafe(compound);
		writeInteractionPoints(compound);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		interactionPointTag = compound.getList("InteractionPoints", Tag.TAG_COMPOUND);
		transferInProgress = compound.getBoolean("TransferInProgress");
		updateInteractionPoints = true;
	}

	@Override
	public void setLevel(Level level) {
		super.setLevel(level);
		for (ArmInteractionPoint input : inputs)
			input.setLevel(level);
		for (ArmInteractionPoint output : outputs)
			output.setLevel(level);
	}

	public static boolean isSelectableFace(LevelAccessor level, BlockPos pos, BlockState state) {
		return true;
	}
}
