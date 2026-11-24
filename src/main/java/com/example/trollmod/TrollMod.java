package com.example.trollmod;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrollMod implements ModInitializer {
	public static final String MOD_ID = "trollmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** 네더/엔드에서 우리가 직접 재운 플레이어들 (바닐라 기상 판정으로부터 보호). */
	public static final Set<UUID> TROLL_SLEEPERS = ConcurrentHashMap.newKeySet();

	@Override
	public void onInitialize() {
		// 기능 A: 네더/엔드 침대 우클릭
		UseBlockCallback.EVENT.register(BedTroll::onUseBlock);

		// 더 이상 자고 있지 않은 플레이어는 보호 목록에서 정리
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (TROLL_SLEEPERS.isEmpty()) {
				return;
			}
			TROLL_SLEEPERS.removeIf(id -> {
				ServerPlayer player = server.getPlayerList().getPlayer(id);
				return player == null || !player.isSleeping();
			});
		});
	}

	public static boolean isTrollSleeper(UUID id) {
		return TROLL_SLEEPERS.contains(id);
	}
}
