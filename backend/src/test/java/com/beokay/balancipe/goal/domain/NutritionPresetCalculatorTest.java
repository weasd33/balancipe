package com.beokay.balancipe.goal.domain;

import com.beokay.balancipe.global.exception.BusinessException;
import com.beokay.balancipe.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NutritionPresetCalculatorTest {

    private final NutritionPresetCalculator calculator = new NutritionPresetCalculator();

    @Test
    void 비율_프리셋_방식으로_매크로를_계산한다() {
        NutritionPresetCalculator.MacroResult result =
                calculator.calculateByRatio(BigDecimal.valueOf(2000), 40, 30, 30);

        assertThat(result.carbohydrate()).isEqualByComparingTo("200.00");
        assertThat(result.protein()).isEqualByComparingTo("150.00");
        assertThat(result.fat()).isEqualByComparingTo("66.67");
    }

    @Test
    void 비율의_합이_100이_아니면_예외를_던진다() {
        assertThatThrownBy(() -> calculator.calculateByRatio(BigDecimal.valueOf(2000), 50, 30, 30))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_NUTRITION_GOAL);
    }

    @Test
    void 체중_기반_방식으로_매크로를_계산한다() {
        NutritionPresetCalculator.MacroResult result = calculator.calculateByWeight(
                BigDecimal.valueOf(2000), BigDecimal.valueOf(70), BigDecimal.valueOf(1.8), BigDecimal.valueOf(0.8));

        assertThat(result.protein()).isEqualByComparingTo("126.00");
        assertThat(result.fat()).isEqualByComparingTo("56.00");
        assertThat(result.carbohydrate()).isEqualByComparingTo("248.00");
    }

    @Test
    void 체중_기반_방식에서_잔여_칼로리가_음수이면_예외를_던진다() {
        assertThatThrownBy(() -> calculator.calculateByWeight(
                BigDecimal.valueOf(1000), BigDecimal.valueOf(70), BigDecimal.valueOf(5), BigDecimal.valueOf(3)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_NUTRITION_GOAL);
    }
}
