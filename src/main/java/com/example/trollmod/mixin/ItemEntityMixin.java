package com.example.trollmod.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 기능 C: 용암 속 다이아몬드는 타 없어지지 않고, 0.4초 뒤 '정제된 다이아몬드'로 변해 플레이어 쪽으로 튀어 오른다. */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
	@Unique
	private static final String TROLLMOD_REFINED_NAME = "정제된 다이아몬드";
	@Unique
	private static final int TROLLMOD_CONVERT_TICKS = 8; // 0.4초

	@Shadow
	private int health;

	@Shadow
	public abstract ItemStack getItem();

	@Shadow
	public abstract void setItem(ItemStack stack);

	@Unique
	private int trollmod$lavaTicks;
	@Unique
	private boolean trollmod$shielded;

	public ItemEntityMixin(EntityType<?> type, Level level) {
		super(type, level);
	}

	// HEAD 에서 무적을 켜야 같은 틱의 baseTick(용암 피해)보다 먼저 막을 수 있다
	@Inject(method = "tick", at = @At("HEAD"))
	private void trollmod$lavaDiamond(CallbackInfo ci) {
		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		ItemStack stack = this.getItem();
		boolean inLava = stack.is(Items.DIAMOND)
				&& (this.isInLava() || serverLevel.getFluidState(this.blockPosition()).is(FluidTags.LAVA));

		if (!inLava) {
			this.trollmod$lavaTicks = 0;
			return;
		}

		// 26.3 에는 Entity#setInvulnerable 이 없으므로, 틱마다 체력을 되돌려 용암 피해(틱당 1회)를 무효화한다
		this.health = 5;
		this.clearFire();

		Component existing = stack.get(DataComponents.CUSTOM_NAME);
		if (existing != null && TROLLMOD_REFINED_NAME.equals(existing.getString())) {
			return; // 이미 정제된 다이아: 용암에서도 안전하되 다시 변환/튀어오르지는 않음
		}
		if (++this.trollmod$lavaTicks < TROLLMOD_CONVERT_TICKS) {
			return;
		}

		// ---- 변환 ----
		ItemStack out = stack.copy();
		out.set(DataComponents.CUSTOM_NAME, Component.literal(TROLLMOD_REFINED_NAME)
				.withStyle(style -> style.withItalic(false).withColor(ChatFormatting.AQUA)));
		out.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, Boolean.TRUE);
		this.setItem(out);

		// 플레이어 쪽으로 톡 튀어 오르기
		double vx = 0.0;
		double vz = 0.0;
		Player nearest = serverLevel.getNearestPlayer(this, 16.0);
		if (nearest != null) {
			Vec3 d = nearest.position().subtract(this.position());
			double h = Math.sqrt(d.x * d.x + d.z * d.z);
			if (h > 1.0E-3) {
				vx = d.x / h * 0.22;
				vz = d.z / h * 0.22;
			}
		}
		this.setDeltaMovement(vx, 0.5, vz);

		serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
				SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.4F);
		serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.2, this.getZ(),
				10, 0.15, 0.1, 0.15, 0.02);
		serverLevel.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 0.2, this.getZ(),
				6, 0.2, 0.1, 0.2, 0.0);

		this.clearFire();
		this.health = 5;
		this.trollmod$lavaTicks = 0;
	}
}
