package com.example.trollmod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * ServerPlayer#setRespawnPosition 은 버전마다 시그니처가 바뀌어 왔다
 * (dimension+pos+angle... → RespawnConfig → RespawnConfig(RespawnData, forced)).
 * 비난독화 환경이라 런타임 리플렉션이 그대로 통하므로, 컴파일이 시그니처에 묶이지 않게 리플렉션으로 호출한다.
 * 실패해도 게임은 계속되고 로그에만 경고가 남는다 (채팅 출력 없음).
 */
final class RespawnHelper {
	private RespawnHelper() {
	}

	static void set(ServerPlayer player, ServerLevel level, BlockPos pos) {
		try {
			for (Method m : ServerPlayer.class.getMethods()) {
				if (!m.getName().equals("setRespawnPosition")) {
					continue;
				}
				Class<?>[] pt = m.getParameterTypes();
				if (pt.length == 2 && pt[1] == boolean.class) {
					Object config = build(pt[0], player, level, pos, 0);
					if (config != null) {
						m.invoke(player, config, false); // showMessage=false (메시지는 따로 보냄)
						return;
					}
				} else if (pt.length == 5) {
					m.invoke(player, level.dimension(), pos, player.getYRot(), false, false);
					return;
				}
			}
			TrollMod.LOGGER.warn("[trollmod] setRespawnPosition 시그니처를 찾지 못했습니다.");
		} catch (Throwable t) {
			TrollMod.LOGGER.warn("[trollmod] 리스폰 지점 설정 실패", t);
		}
	}

	private static Object build(Class<?> type, ServerPlayer player, ServerLevel level, BlockPos pos, int depth)
			throws Exception {
		if (depth > 3) {
			return null;
		}
		for (Constructor<?> c : type.getDeclaredConstructors()) {
			Class<?>[] pt = c.getParameterTypes();
			Object[] args = new Object[pt.length];
			int floats = 0;
			boolean ok = true;
			for (int i = 0; i < pt.length && ok; i++) {
				Class<?> t = pt[i];
				if (t == ResourceKey.class) {
					args[i] = level.dimension();
				} else if (t == BlockPos.class) {
					args[i] = pos;
				} else if (t == GlobalPos.class) {
					args[i] = GlobalPos.of(level.dimension(), pos);
				} else if (t == float.class) {
					args[i] = (floats++ == 0) ? player.getYRot() : player.getXRot();
				} else if (t == boolean.class) {
					args[i] = Boolean.FALSE;
				} else {
					args[i] = build(t, player, level, pos, depth + 1);
					ok = args[i] != null;
				}
			}
			if (ok) {
				c.setAccessible(true);
				return c.newInstance(args);
			}
		}
		return null;
	}
}
