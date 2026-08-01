package com.beokay.balancipe.goal.dto;

import com.beokay.balancipe.goal.domain.ActivityLevel;
import com.beokay.balancipe.goal.domain.MacroCalculationMethod;
import com.beokay.balancipe.goal.domain.MacroPresetType;
import com.beokay.balancipe.goal.domain.UserNutritionGoal;
import com.beokay.balancipe.nutrition.domain.PregnancyStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record NutritionGoalResponse(

        @Schema(description = "임신/수유 상태", example = "NONE")
        PregnancyStatus pregnancyStatus,

        @Schema(description = "식이섬유 목표 산출에 사용한 KDRI 기준 연도", example = "2025")
        int referenceYear,

        @Schema(description = "목표 칼로리(kcal)", example = "2000.00")
        BigDecimal targetCalorie,

        @Schema(description = "매크로 계산 방식", example = "RATIO_PRESET")
        MacroCalculationMethod macroCalculationMethod,

        @Schema(description = "비율 프리셋 종류(RATIO_PRESET일 때만 값 존재)", example = "KETOGENIC")
        MacroPresetType macroPresetType,

        @Schema(description = "키(cm)", example = "170.00")
        BigDecimal heightCm,

        @Schema(description = "현재 체중(kg)", example = "70.00")
        BigDecimal currentWeightKg,

        @Schema(description = "목표 체중(kg)", example = "65.00")
        BigDecimal targetWeightKg,

        @Schema(description = "활동량 수준", example = "MODERATE")
        ActivityLevel activityLevel,

        @Schema(description = "주당 목표 체중 변화율(kg/주)", example = "0.50")
        BigDecimal weeklyRateKg,

        @Schema(description = "체중 kg당 단백질 목표(g, WEIGHT_BASED_FORMULA일 때만 값 존재)", example = "1.80")
        BigDecimal proteinPerKg,

        @Schema(description = "체중 kg당 지방 목표(g, WEIGHT_BASED_FORMULA일 때만 값 존재)", example = "0.80")
        BigDecimal fatPerKg,

        @Schema(description = "탄수화물 목표(g)", example = "50.00")
        BigDecimal targetCarbohydrate,

        @Schema(description = "단백질 목표(g)", example = "150.00")
        BigDecimal targetProtein,

        @Schema(description = "지방 목표(g)", example = "133.30")
        BigDecimal targetFat,

        @Schema(description = "식이섬유 목표(g)", example = "25.00")
        BigDecimal targetDietaryFiber
) {
    public static NutritionGoalResponse from(UserNutritionGoal goal) {
        return new NutritionGoalResponse(
                goal.getPregnancyStatus(),
                goal.getReferenceYear(),
                goal.getTargetCalorie(),
                goal.getMacroCalculationMethod(),
                goal.getMacroPresetType(),
                goal.getHeightCm(),
                goal.getCurrentWeightKg(),
                goal.getTargetWeightKg(),
                goal.getActivityLevel(),
                goal.getWeeklyRateKg(),
                goal.getProteinPerKg(),
                goal.getFatPerKg(),
                goal.getTargetCarbohydrate(),
                goal.getTargetProtein(),
                goal.getTargetFat(),
                goal.getTargetDietaryFiber()
        );
    }
}
