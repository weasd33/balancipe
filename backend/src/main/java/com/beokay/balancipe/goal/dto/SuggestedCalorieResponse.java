package com.beokay.balancipe.goal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record SuggestedCalorieResponse(

        @Schema(description = "제안 목표 칼로리(kcal), 저장되지 않는 미리보기 값", example = "2000.00")
        BigDecimal suggestedCalorie
) {
    public static SuggestedCalorieResponse of(BigDecimal suggestedCalorie) {
        return new SuggestedCalorieResponse(suggestedCalorie);
    }
}
