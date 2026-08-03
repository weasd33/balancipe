package com.beokay.balancipe.goal.domain;

import com.beokay.balancipe.user.domain.Gender;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CalorieGoalCalculatorTest {

    private final CalorieGoalCalculator calculator = new CalorieGoalCalculator();

    @Test
    void 감량_목표_칼로리를_계산한다() {
        BigDecimal result = calculator.calculate(
                Gender.FEMALE, 30, BigDecimal.valueOf(165), BigDecimal.valueOf(60), BigDecimal.valueOf(55),
                ActivityLevel.MODERATE, BigDecimal.valueOf(0.5));

        assertThat(result).isEqualByComparingTo("1496.39");
    }

    @Test
    void 증량_목표_칼로리를_계산한다() {
        BigDecimal result = calculator.calculate(
                Gender.MALE, 25, BigDecimal.valueOf(175), BigDecimal.valueOf(70), BigDecimal.valueOf(75),
                ActivityLevel.ACTIVE, BigDecimal.valueOf(0.25));

        assertThat(result).isEqualByComparingTo("3162.22");
    }

    @Test
    void 목표_체중이_현재_체중과_같으면_유지_칼로리를_계산한다() {
        BigDecimal result = calculator.calculate(
                Gender.MALE, 40, BigDecimal.valueOf(170), BigDecimal.valueOf(68), BigDecimal.valueOf(68),
                ActivityLevel.SEDENTARY, BigDecimal.valueOf(0.5));

        assertThat(result).isEqualByComparingTo("1857.00");
    }
}
