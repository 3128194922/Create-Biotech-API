package com.nobodiiiii.createbiotech.content.shulkerpackager;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.PackagerItemHandler;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class ShulkerPackagerItemHandler extends PackagerItemHandler {

	private final ShulkerPackagerBlockEntity blockEntity;

	public ShulkerPackagerItemHandler(ShulkerPackagerBlockEntity blockEntity) {
		super(blockEntity);
		this.blockEntity = blockEntity;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!isItemValid(slot, stack))
			return stack;
		if (!blockEntity.canAcceptTransferredPackage(stack))
			return stack;
		if (simulate)
			return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1);

		ItemStack inserted = stack.copy();
		inserted.setCount(1);
		if (!blockEntity.startIncomingTransfer(inserted))
			return stack;
		return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return PackageItem.isPackage(stack);
	}
}
