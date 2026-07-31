package com.beokay.balancipe.nutrition.dto;

import com.beokay.balancipe.nutrition.domain.AgeGroup;
import com.beokay.balancipe.nutrition.domain.IndicatorType;
import com.beokay.balancipe.nutrition.domain.KoreanDietaryReference;
import com.beokay.balancipe.nutrition.domain.NutrientCode;
import com.beokay.balancipe.nutrition.domain.PregnancyStatus;
import com.beokay.balancipe.user.domain.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

// /reference/all은 기준값과 임신/수유 부가값을 합산하지 않고 원본 행을 그대로 그리드로 노출한다.
public record NutritionReferenceAllItemResponse(

    @Schema(description = "성별", example = "FEMALE")
    Gender gender,

    @Schema(description = "연령대", example = "ADULT_19_29Y")
    AgeGroup ageGroup,

    @Schema(description = "임신/수유 상태(NONE=기준값, 그 외=가산값)", example = "NONE")
    PregnancyStatus pregnancyStatus,

    @Schema(description = "영양소 코드", example = "CALCIUM")
    NutrientCode nutrientCode,

    @Schema(description = "단위", example = "mg")
    String unit,

    @Schema(description = "지표 유형(EAR/RDA/AI/UL)", example = "RDA")
    IndicatorType indicatorType,

    @Schema(description = "값(NONE 행=기준값, 그 외 행=가산값)", example = "700.000")
    BigDecimal value
) {
    public static NutritionReferenceAllItemResponse from(KoreanDietaryReference reference) {
        return new NutritionReferenceAllItemResponse(
            reference.getGender(),
            reference.getAgeGroup(),
            reference.getPregnancyStatus(),
            reference.getNutrientCode(),
            reference.getNutrientCode().getUnit(),
            reference.getIndicatorType(),
            reference.getValue()
        );
    }
}
