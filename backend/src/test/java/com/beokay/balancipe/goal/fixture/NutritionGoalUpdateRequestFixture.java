package com.beokay.balancipe.goal.fixture;

import com.beokay.balancipe.goal.domain.ActivityLevel;
import com.beokay.balancipe.goal.domain.MacroCalculationMethod;
import com.beokay.balancipe.goal.domain.MacroPresetType;
import com.beokay.balancipe.goal.dto.NutritionGoalUpdateRequest;
import com.beokay.balancipe.nutrition.domain.PregnancyStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public enum NutritionGoalUpdateRequestFixture {

    VALID_RATIO_PRESET_REQUEST(
            new NutritionGoalUpdateRequest(
                    PregnancyStatus.NONE, BigDecimal.valueOf(170), BigDecimal.valueOf(70), BigDecimal.valueOf(65),
                    ActivityLevel.MODERATE, BigDecimal.valueOf(0.5), BigDecimal.valueOf(2000),
                    MacroCalculationMethod.RATIO_PRESET, MacroPresetType.KETOGENIC,
                    null, null, null, null, null)
    ),

    VALID_WEIGHT_BASED_REQUEST(
            new NutritionGoalUpdateRequest(
                    PregnancyStatus.NONE, BigDecimal.valueOf(170), BigDecimal.valueOf(70), BigDecimal.valueOf(65),
                    ActivityLevel.MODERATE, BigDecimal.valueOf(0.5), BigDecimal.valueOf(2000),
                    MacroCalculationMethod.WEIGHT_BASED_FORMULA, null,
                    null, null, null, BigDecimal.valueOf(1.8), BigDecimal.valueOf(0.8))
    ),

    MISSING_HEIGHT_REQUEST(
            new NutritionGoalUpdateRequest(
                    PregnancyStatus.NONE, null, BigDecimal.valueOf(70), BigDecimal.valueOf(65),
                    ActivityLevel.MODERATE, BigDecimal.valueOf(0.5), BigDecimal.valueOf(2000),
                    MacroCalculationMethod.RATIO_PRESET, MacroPresetType.KETOGENIC,
                    null, null, null, null, null)
    ),

    NEGATIVE_TARGET_CALORIE_REQUEST(
            new NutritionGoalUpdateRequest(
                    PregnancyStatus.NONE, BigDecimal.valueOf(170), BigDecimal.valueOf(70), BigDecimal.valueOf(65),
                    ActivityLevel.MODERATE, BigDecimal.valueOf(0.5), BigDecimal.valueOf(-100),
                    MacroCalculationMethod.RATIO_PRESET, MacroPresetType.KETOGENIC,
                    null, null, null, null, null)
    );

    private final NutritionGoalUpdateRequest request;
}
