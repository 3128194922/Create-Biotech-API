package com.nobodiiiii.createbiotech.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.nobodiiiii.createbiotech.content.processing.basin.BasinEntityProcessingRecipe;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.foundation.recipe.RecipeFinder;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

@Mixin(BasinOperatingBlockEntity.class)
public abstract class BasinOperatingBlockEntityMixin {

	private static final Object basinEntityProcessingRecipesKey = new Object();

	@Shadow(remap = false)
	protected abstract <C extends Container> boolean matchBasinRecipe(Recipe<C> recipe);

	@Shadow(remap = false)
	protected abstract <C extends Container> boolean matchStaticFilters(Recipe<C> recipe);

	@Inject(method = "getMatchingRecipes", at = @At("RETURN"), cancellable = true, remap = false)
	private void createBiotech$addEntityProcessingRecipes(CallbackInfoReturnable<List<Recipe<?>>> cir) {
		List<Recipe<?>> recipes = cir.getReturnValue();
		Level level = ((BasinOperatingBlockEntity) (Object) this).getLevel();
		if (level == null)
			return;

		for (Recipe<?> recipe : RecipeFinder.get(basinEntityProcessingRecipesKey, level,
			r -> r instanceof BasinEntityProcessingRecipe)) {
			if (!recipes.contains(recipe) && matchStaticFilters(recipe) && matchBasinRecipe(recipe))
				recipes.add(recipe);
		}

		cir.setReturnValue(recipes);
	}
}
