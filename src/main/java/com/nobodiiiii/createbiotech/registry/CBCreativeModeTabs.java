package com.nobodiiiii.createbiotech.registry;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.mutable.MutableObject;

import com.nobodiiiii.createbiotech.CreateBiotech;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CBCreativeModeTabs {

	private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
		DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateBiotech.MOD_ID);

	public static final RegistryObject<CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main",
		() -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.create_biotech.main"))
			.withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
			.icon(() -> CBItems.EVOKER_ENCHANTING_CHAMBER.get()
				.getDefaultInstance())
			.displayItems(new AutoDisplayItemsGenerator())
			.build());

	private CBCreativeModeTabs() {}

	public static void register(IEventBus modEventBus) {
		CREATIVE_MODE_TABS.register(modEventBus);
	}

	private static class AutoDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {
		private static final Predicate<Item> IS_ITEM_3D_PREDICATE;

		static {
			MutableObject<Predicate<Item>> isItem3d = new MutableObject<>(item -> false);
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
				isItem3d.setValue(item -> {
					ItemRenderer itemRenderer = Minecraft.getInstance()
						.getItemRenderer();
					BakedModel model = itemRenderer.getModel(new ItemStack(item), null, null, 0);
					return model.isGui3d();
				});
			});
			IS_ITEM_3D_PREDICATE = isItem3d.getValue();
		}

		@Override
		public void accept(ItemDisplayParameters parameters, Output output) {
			Predicate<Item> exclusionPredicate = makeExclusionPredicate();
			Function<Item, TabVisibility> visibilityFunc = makeVisibilityFunc();
			List<Item> items = new ArrayList<>();

			items.addAll(collectItems(exclusionPredicate.or(IS_ITEM_3D_PREDICATE.negate())));
			items.addAll(collectBlocks(exclusionPredicate));
			items.addAll(collectItems(exclusionPredicate.or(IS_ITEM_3D_PREDICATE)));

			outputAll(output, items, visibilityFunc);
		}

		private static Predicate<Item> makeExclusionPredicate() {
			Set<Item> exclusions = Set.of(
				CBItems.EXPERIENCE.get(),
				CBItems.CAPTURED_SMALL_SLIME.get(),
				CBItems.INCOMPLETE_CREEPER_BLAST_CHAMBER.get(),
				CBItems.INCOMPLETE_BIONIC_MECHANISM.get()
			);
			return exclusions::contains;
		}

		private static Function<Item, TabVisibility> makeVisibilityFunc() {
			Map<Item, TabVisibility> visibilities = new IdentityHashMap<>();

			for (DyeColor color : DyeColor.values()) {
				if (color == DyeColor.RED) {
					continue;
				}
				visibilities.put(CBItems.BUFFER_PADS.get(color).get(), TabVisibility.SEARCH_TAB_ONLY);
			}

			return item -> visibilities.getOrDefault(item, TabVisibility.PARENT_AND_SEARCH_TABS);
		}

		private static List<Item> collectBlocks(Predicate<Item> exclusionPredicate) {
			List<Item> items = new ArrayList<>();

			for (Block block : ForgeRegistries.BLOCKS.getValues()) {
				ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
				if (!isOwnEntry(key)) {
					continue;
				}

				Item item = block.asItem();
				if (item == Items.AIR || exclusionPredicate.test(item)) {
					continue;
				}

				items.add(item);
			}

			return items;
		}

		private static List<Item> collectItems(Predicate<Item> exclusionPredicate) {
			List<Item> items = new ArrayList<>();

			for (Item item : ForgeRegistries.ITEMS.getValues()) {
				ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
				if (!isOwnEntry(key)) {
					continue;
				}
				if (item instanceof BlockItem || exclusionPredicate.test(item)) {
					continue;
				}

				items.add(item);
			}

			return items;
		}

		private static boolean isOwnEntry(ResourceLocation key) {
			return key != null && CreateBiotech.MOD_ID.equals(key.getNamespace());
		}

		private static void outputAll(Output output, List<Item> items, Function<Item, TabVisibility> visibilityFunc) {
			for (Item item : items) {
				output.accept(item.getDefaultInstance(), visibilityFunc.apply(item));
			}
		}
	}
}
