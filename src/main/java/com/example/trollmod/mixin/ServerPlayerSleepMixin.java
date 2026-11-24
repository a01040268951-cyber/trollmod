package com.example.trollmod.mixin;

import com.example.trollmod.TrollMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.3 에서는 침대 규칙이 환경 속성(minecraft:gameplay/bed_rule)이라, 네더/엔드(can_sleep = never)에서 재우면
 * Player#tick 이 다음 틱에 곧바로 깨워 버린다. 우리가 재운 플레이어에 한해 '자동 기상'만 막고,
 * 클라이언트의 '침대에서 일어나기' 패킷(ServerGamePacketListenerImpl 경유)은 그대로 통과시킨다.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerSleepMixin {
	@Inject(method = "stopSleepInBed", at = @At("HEAD"), cancellable = true)
	private void trollmod$keepSleeping(boolean wakeImmediately, boolean updateLevelForSleepingPlayers, CallbackInfo ci) {
		ServerPlayer self = (ServerPlayer) (Object) this;
		if (!TrollMod.isTrollSleeper(self.getUUID())) {
			return;
		}
		boolean inTrollDimension = self.level().dimension().equals(Level.NETHER)
				|| self.level().dimension().equals(Level.END);
		if (!inTrollDimension) {
			TrollMod.TROLL_SLEEPERS.remove(self.getUUID());
			return;
		}
		if (trollmod$calledByClientPacket()) {
			TrollMod.TROLL_SLEEPERS.remove(self.getUUID()); // 플레이어가 직접 '일어나기'를 누름
			return;
		}
		ci.cancel(); // 바닐라의 자동 기상 차단
	}

	private static boolean trollmod$calledByClientPacket() {
		return StackWalker.getInstance().walk(frames ->
				frames.anyMatch(f -> f.getClassName().contains("ServerGamePacketListener")));
	}
}
