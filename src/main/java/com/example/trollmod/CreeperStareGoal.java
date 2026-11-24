package com.example.trollmod;

import java.util.EnumSet;

import com.example.trollmod.mixin.CreeperAccessor;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 기능 B: 플레이어와 눈이 마주치면 (8블록 이내, 시선 오차 18° 이내) 멈춰 서서 쳐다보고 swell 을 0으로 초기화.
 * 우선순위 0 + MOVE/LOOK 플래그 → 추적(Melee)·점화(Swell) 목표를 바닐라 방식 그대로 밀어낸다.
 * 시선이 벗어나면 canContinueToUse 가 false → 자동으로 기존 AI 복귀.
 */
public class CreeperStareGoal extends Goal {
	private static final double MAX_DISTANCE = 8.0;
	private static final double MIN_DOT = Math.cos(Math.toRadians(18.0));

	private final Creeper creeper;
	private Player watcher;

	public CreeperStareGoal(Creeper creeper) {
		this.creeper = creeper;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		// 추적 중이거나 부풀어 오르는 중일 때만 발동
		boolean aggro = creeper.getTarget() != null
				|| creeper.getSwellDir() > 0
				|| ((CreeperAccessor) creeper).trollmod$getSwell() > 0;
		if (!aggro) {
			return false;
		}
		watcher = findWatcher();
		return watcher != null;
	}

	@Override
	public boolean canContinueToUse() {
		watcher = findWatcher();
		return watcher != null;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void start() {
		creeper.getNavigation().stop();
	}

	@Override
	public void tick() {
		if (watcher == null) {
			return;
		}
		creeper.getNavigation().stop();
		// MoveControl 이 남은 목적지로 계속 끌고 가지 않도록 현재 위치를 목표로 덮어쓴다
		creeper.getMoveControl().setWantedPosition(creeper.getX(), creeper.getY(), creeper.getZ(), 0.0);
		creeper.getLookControl().setLookAt(watcher, 30.0F, 30.0F);

		// 폭발 카운트다운 즉시 0 으로
		creeper.setSwellDir(-1);
		CreeperAccessor acc = (CreeperAccessor) creeper;
		acc.trollmod$setSwell(0);
		acc.trollmod$setOldSwell(0);
	}

	@Override
	public void stop() {
		watcher = null;
	}

	private Player findWatcher() {
		Vec3 target = creeper.getBoundingBox().getCenter();
		for (Player p : creeper.level().players()) {
			if (p.isSpectator() || !p.isAlive()) {
				continue;
			}
			if (p.distanceTo(creeper) > MAX_DISTANCE) {
				continue;
			}
			Vec3 toCreeper = target.subtract(p.getEyePosition());
			double len = toCreeper.length();
			if (len < 1.0E-4) {
				continue;
			}
			double dot = p.getViewVector(1.0F).dot(toCreeper.scale(1.0 / len));
			if (dot < MIN_DOT) {
				continue;
			}
			if (!p.hasLineOfSight(creeper)) {
				continue;
			}
			return p;
		}
		return null;
	}
}
