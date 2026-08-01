package com.beokay.balancipe.goal.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityLevel {

    SEDENTARY(1.2),      // 좌식 생활, 운동 거의 안 함
    LIGHT(1.375),        // 가벼운 활동, 주 1-3회 가벼운 운동
    MODERATE(1.55),      // 보통 활동, 주 3-5회 운동
    ACTIVE(1.725),       // 활발한 활동, 주 6-7회 운동
    VERY_ACTIVE(1.9);    // 매우 활발한 활동, 매일 강도 높은 운동/육체노동

    private final double factor; // BMR에 곱해 TDEE(활동대사량) 산출
}
