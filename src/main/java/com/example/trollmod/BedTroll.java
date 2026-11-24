package com.example.trollmod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;

/** 기능 A: 네더/엔드에서 침대가 폭발하지 않고 정상 취침 + 스폰 지점 설정. */
public final class BedTroll {
	private BedTroll() {
	}

	public static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
		// 서버에서만 처리. 클라이언트는 바닐라 흐름(PASS)에 맡김.
		if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer sp)) {
			return InteractionResult.PASS;
		}
		if (!(level.dimension().equals(Level.NETHER) || level.dimension().equals(Level.END))) {
			return InteractionResult.PASS;
		}
		if (sp.isSpectator()) {
			return InteractionResult.PASS;
		}

		BlockPos pos = hit.getBlockPos();
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof BedBlock)) {
			return InteractionResult.PASS;
		}

		// 바닐라와 동일: 웅크리고 손에 아이템이 있으면 블록 상호작용 생략
		if (sp.isSecondaryUseActive() && (!sp.getMainHandItem().isEmpty() || !sp.getOffhandItem().isEmpty())) {
			return InteractionResult.PASS;
		}

		// 침대 발 쪽을 눌렀다면 머리 쪽 위치로 보정
		if (state.getValue(BedBlock.PART) != BedPart.HEAD) {
			pos = pos.relative(state.getValue(BedBlock.FACING));
			state = level.getBlockState(pos);
			if (!(state.getBlock() instanceof BedBlock)) {
				return InteractionResult.CONSUME;
			}
		}

		if (state.getValue(BedBlock.OCCUPIED)) {
			sp.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"), true);
			return InteractionResult.SUCCESS;
		}
		if (sp.isSleeping()) {
			return InteractionResult.CONSUME;
		}

		// 1) 리스폰 지점 설정 (메시지는 직접 보냄 → 한국어 클라이언트는 "리스폰 지점이 설정되었습니다")
		RespawnHelper.set(sp, serverLevel, pos);
		sp.sendSystemMessage(Component.translatable("block.minecraft.set_spawn"));

		// 2) 바닐라 기상 판정으로부터 보호 목록에 등록 후 취침
		//    startSleeping() 은 시간/차원 검사를 거치지 않는 저수준 취침 → 폭발 경로 완전 우회
		TrollMod.TROLL_SLEEPERS.add(sp.getUUID());
		sp.startSleeping(pos);
		return InteractionResult.SUCCESS;
	}
}
