package com.beokay.balancipe.goal.dto;

import com.beokay.balancipe.goal.domain.ActivityLevel;
import com.beokay.balancipe.goal.domain.MacroCalculationMethod;
import com.beokay.balancipe.goal.domain.MacroPresetType;
import com.beokay.balancipe.nutrition.domain.PregnancyStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record NutritionGoalUpdateRequest(

        @Schema(description = "임신/수유 상태", example = "NONE")
        @NotNull(message = "임신/수유 상태를 입력해주세요.")
        PregnancyStatus pregnancyStatus,

        @Schema(description = "키(cm)", example = "170.00")
        @NotNull(message = "키를 입력해주세요.")
        @Positive(message = "키는 0보다 커야 합니다.")
        BigDecimal heightCm,

        @Schema(description = "현재 체중(kg)", example = "70.00")
        @NotNull(message = "현재 체중을 입력해주세요.")
        @Positive(message = "현재 체중은 0보다 커야 합니다.")
        BigDecimal currentWeightKg,

        @Schema(description = "목표 체중(kg)", example = "65.00")
        @NotNull(message = "목표 체중을 입력해주세요.")
        @Positive(message = "목표 체중은 0보다 커야 합니다.")
        BigDecimal targetWeightKg,

        @Schema(description = "활동량 수준", example = "MODERATE")
        @NotNull(message = "활동량 수준을 입력해주세요.")
        ActivityLevel activityLevel,

        @Schema(description = "주당 목표 체중 변화율(kg/주)", example = "0.50")
        @NotNull(message = "주당 목표 체중 변화율을 입력해주세요.")
        @PositiveOrZero(message = "주당 목표 체중 변화율은 0 이상이어야 합니다.")
        BigDecimal weeklyRateKg,

        @Schema(description = "목표 칼로리(kcal), 서버 재계산 없이 그대로 저장됨", example = "2000.00")
        @NotNull(message = "목표 칼로리를 입력해주세요.")
        @Positive(message = "목표 칼로리는 0보다 커야 합니다.")
        BigDecimal targetCalorie,

        @Schema(description = "매크로 계산 방식", example = "RATIO_PRESET")
        @NotNull(message = "매크로 계산 방식을 입력해주세요.")
        MacroCalculationMethod macroCalculationMethod,

        @Schema(description = "비율 프리셋 종류(RATIO_PRESET일 때 필수)", example = "KETOGENIC")
        MacroPresetType macroPresetType,

        @Schema(description = "탄수화물 비율(macroPresetType=CUSTOM일 때 필수, 셋의 합이 100)", example = "40")
        Integer customCarbRatio,

        @Schema(description = "단백질 비율(macroPresetType=CUSTOM일 때 필수, 셋의 합이 100)", example = "30")
        Integer customProteinRatio,

        @Schema(description = "지방 비율(macroPresetType=CUSTOM일 때 필수, 셋의 합이 100)", example = "30")
        Integer customFatRatio,

        @Schema(description = "체중 kg당 단백질 목표(g, WEIGHT_BASED_FORMULA일 때 필수)", example = "1.80")
        BigDecimal proteinPerKg,

        @Schema(description = "체중 kg당 지방 목표(g, WEIGHT_BASED_FORMULA일 때 필수)", example = "0.80")
        BigDecimal fatPerKg
) {
}
