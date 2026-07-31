package com.beokay.balancipe.nutrition.dto;

import com.beokay.balancipe.nutrition.domain.IndicatorType;
import com.beokay.balancipe.nutrition.domain.NutrientCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record NutritionReferenceItemResponse(

    @Schema(description = "영양소 코드", example = "CALCIUM")
    NutrientCode nutrientCode,

    @Schema(description = "단위", example = "mg")
    String unit,

    @Schema(description = "지표 유형(EAR/RDA/AI/UL)", example = "RDA")
    IndicatorType indicatorType,

    @Schema(description = "기준값(임신/수유 부가량 합산 완료)", example = "700.000")
    BigDecimal value
) {
    public static NutritionReferenceItemResponse of(NutrientCode nutrientCode, IndicatorType indicatorType, BigDecimal value) {
        return new NutritionReferenceItemResponse(nutrientCode, nutrientCode.getUnit(), indicatorType, value);
    }
}
