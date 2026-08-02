package com.beokay.balancipe.goal.domain;

import com.beokay.balancipe.global.exception.BusinessException;
import com.beokay.balancipe.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/*
    목표 칼로리를 탄수화물/단백질/지방 목표(g)로 분해한다.
    방식 A(비율): 탄:단:지 비율(합 100) × 칼로리 ÷ 열량계수(탄4/단4/지9)
    방식 B(체중 기반): 체중(kg) × g/kg으로 단백질·지방을 먼저 정하고, 남은 칼로리를 탄수화물로 채운다.
 */
@Component
public class NutritionPresetCalculator {

    private static final BigDecimal RATIO_TOTAL = BigDecimal.valueOf(100);
    private static final BigDecimal CARB_KCAL_PER_GRAM = BigDecimal.valueOf(4);
    private static final BigDecimal PROTEIN_KCAL_PER_GRAM = BigDecimal.valueOf(4);
    private static final BigDecimal FAT_KCAL_PER_GRAM = BigDecimal.valueOf(9);

    public MacroResult calculateByRatio(BigDecimal targetCalorie, int carbRatio, int proteinRatio, int fatRatio) {
        if (carbRatio + proteinRatio + fatRatio != 100) {
            throw new BusinessException(ErrorCode.INVALID_NUTRITION_GOAL);
        }

        BigDecimal carbG = gramsFromRatio(targetCalorie, carbRatio, CARB_KCAL_PER_GRAM);
        BigDecimal proteinG = gramsFromRatio(targetCalorie, proteinRatio, PROTEIN_KCAL_PER_GRAM);
        BigDecimal fatG = gramsFromRatio(targetCalorie, fatRatio, FAT_KCAL_PER_GRAM);
        return new MacroResult(carbG, proteinG, fatG);
    }

    public MacroResult calculateByWeight(BigDecimal targetCalorie, BigDecimal currentWeightKg,
                                          BigDecimal proteinPerKg, BigDecimal fatPerKg) {
        BigDecimal proteinG = currentWeightKg.multiply(proteinPerKg).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fatG = currentWeightKg.multiply(fatPerKg).setScale(2, RoundingMode.HALF_UP);

        BigDecimal remainingCalorie = targetCalorie
            .subtract(proteinG.multiply(PROTEIN_KCAL_PER_GRAM))
            .subtract(fatG.multiply(FAT_KCAL_PER_GRAM));
        if (remainingCalorie.signum() < 0) {
            throw new BusinessException(ErrorCode.INVALID_NUTRITION_GOAL);
        }

        BigDecimal carbG = remainingCalorie.divide(CARB_KCAL_PER_GRAM, 2, RoundingMode.HALF_UP);
        return new MacroResult(carbG, proteinG, fatG);
    }

    private BigDecimal gramsFromRatio(BigDecimal targetCalorie, int ratio, BigDecimal kcalPerGram) {
        return targetCalorie.multiply(BigDecimal.valueOf(ratio))
            .divide(RATIO_TOTAL, 4, RoundingMode.HALF_UP)
            .divide(kcalPerGram, 2, RoundingMode.HALF_UP);
    }

    public record MacroResult(BigDecimal carbohydrate, BigDecimal protein, BigDecimal fat) {
    }
}
