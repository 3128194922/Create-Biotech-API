package com.nobodiiiii.createbiotech.content.biopackager;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.annotation.Nullable;

import com.simibubi.create.AllDamageTypes;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class BioPackagerContraptionDamageTracker {

	private static final ThreadLocal<Deque<DamageContext>> ACTIVE_DAMAGE =
		ThreadLocal.withInitial(ArrayDeque::new);

	private BioPackagerContraptionDamageTracker() {}

	public static void pushDamageContext(AbstractContraptionEntity contraptionEntity) {
		ACTIVE_DAMAGE.get()
			.push(new DamageContext(null, contraptionEntity));
	}

	public static boolean pushDamageContext(Entity target, AbstractContraptionEntity contraptionEntity) {
		if (!(target instanceof LivingEntity livingTarget))
			return false;
		ACTIVE_DAMAGE.get()
			.push(new DamageContext(livingTarget, contraptionEntity));
		return true;
	}

	public static void popDamageContext() {
		Deque<DamageContext> stack = ACTIVE_DAMAGE.get();
		if (!stack.isEmpty())
			stack.pop();
		if (stack.isEmpty())
			ACTIVE_DAMAGE.remove();
	}

	@Nullable
	public static AbstractContraptionEntity resolveDamagingContraption(LivingEntity target, DamageSource source) {
		if (source == null)
			return null;

		AbstractContraptionEntity contraptionFromSource = asContraption(source.getDirectEntity());
		if (contraptionFromSource == null)
			contraptionFromSource = asContraption(source.getEntity());
		if (contraptionFromSource != null && !contraptionFromSource.isRemoved())
			return contraptionFromSource;

		if (!isContraptionDamageType(source))
			return null;

		DamageContext context = ACTIVE_DAMAGE.get()
			.peek();
		if (context == null)
			return null;
		if (context.target() != null && context.target() != target)
			return null;
		AbstractContraptionEntity contraptionEntity = context.contraptionEntity();
		if (contraptionEntity == null || contraptionEntity.isRemoved())
			return null;
		return contraptionEntity;
	}

	private static boolean isContraptionDamageType(DamageSource source) {
		return source.is(AllDamageTypes.CRUSH)
			|| source.is(AllDamageTypes.DRILL)
			|| source.is(AllDamageTypes.ROLLER)
			|| source.is(AllDamageTypes.SAW)
			|| source.is(AllDamageTypes.RUN_OVER);
	}

	@Nullable
	private static AbstractContraptionEntity asContraption(@Nullable Entity entity) {
		return entity instanceof AbstractContraptionEntity contraptionEntity ? contraptionEntity : null;
	}

	private record DamageContext(@Nullable LivingEntity target, AbstractContraptionEntity contraptionEntity) {}
}
