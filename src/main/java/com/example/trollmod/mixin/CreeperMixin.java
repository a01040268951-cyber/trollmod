package com.example.trollmod.mixin;

import com.example.trollmod.CreeperStareGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster {
	protected CreeperMixin(EntityType<? extends Monster> type, Level level) {
		super(type, level);
	}

	@Inject(method = "registerGoals", at = @At("TAIL"))
	private void trollmod$addStareGoal(CallbackInfo ci) {
		this.goalSelector.addGoal(0, new CreeperStareGoal((Creeper) (Object) this));
	}
}
