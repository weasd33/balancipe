package com.beokay.balancipe.goal.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MacroPresetType {

    ZONE(40, 30, 30),               // 존 다이어트
    KETOGENIC(10, 30, 60),          // 키토제닉
    LOW_FAT_HIGH_CARB(60, 20, 20),  // 저지방 고탄수
    WEIGHT_TRAINING(30, 40, 30),    // 웨이트 트레이닝
    CLASSIC_BULK(50, 30, 20),       // 클래식 벌크업
    LEAN_BULK(40, 40, 20),          // 린매스업
    CUSTOM(null, null, null);       // 비율 직접 선택

    private final Integer carbRatio;
    private final Integer proteinRatio;
    private final Integer fatRatio;
}
