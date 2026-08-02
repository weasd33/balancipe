package com.beokay.balancipe.goal.domain;

import com.beokay.balancipe.user.domain.Gender;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/*
    Mifflin-St Jeor 공식으로 BMR을 구하고 활동계수를 곱해 TDEE를 산출한 뒤,
    목표 체중 변화율(주당 kg)을 7700kcal/kg 근사치로 환산해 가감한 목표 칼로리를 제안한다.
 */
@Component
public class CalorieGoalCalculator {

    private static final BigDecimal MALE_CONSTANT = BigDecimal.valueOf(5);
    private static final BigDecimal FEMALE_CONSTANT = BigDecimal.valueOf(161);
    private static final BigDecimal WEIGHT_COEFFICIENT = BigDecimal.valueOf(10);
    private static final BigDecimal HEIGHT_COEFFICIENT = BigDecimal.valueOf(6.25);
    private static final BigDecimal AGE_COEFFICIENT = BigDecimal.valueOf(5);
    private static final BigDecimal KCAL_PER_KG = BigDecimal.valueOf(7700);
    private static final BigDecimal DAYS_PER_WEEK = BigDecimal.valueOf(7);

    public BigDecimal calculate(Gender gender, int ageYears, BigDecimal heightCm, BigDecimal currentWeightKg,
                                 BigDecimal targetWeightKg, ActivityLevel activityLevel, BigDecimal weeklyRateKg) {
        BigDecimal bmr = calculateBmr(gender, ageYears, heightCm, currentWeightKg);
        BigDecimal tdee = bmr.multiply(BigDecimal.valueOf(activityLevel.getFactor()));
        BigDecimal dailyAdjustment = calculateDailyAdjustment(currentWeightKg, targetWeightKg, weeklyRateKg);
        return tdee.add(dailyAdjustment).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateBmr(Gender gender, int ageYears, BigDecimal heightCm, BigDecimal currentWeightKg) {
        BigDecimal base = WEIGHT_COEFFICIENT.multiply(currentWeightKg)
            .add(HEIGHT_COEFFICIENT.multiply(heightCm))
            .subtract(AGE_COEFFICIENT.multiply(BigDecimal.valueOf(ageYears)));
        return gender == Gender.MALE ? base.add(MALE_CONSTANT) : base.subtract(FEMALE_CONSTANT);
    }

    private BigDecimal calculateDailyAdjustment(BigDecimal currentWeightKg, BigDecimal targetWeightKg, BigDecimal weeklyRateKg) {
        int direction = targetWeightKg.compareTo(currentWeightKg);
        if (direction == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal dailyRate = weeklyRateKg.multiply(KCAL_PER_KG).divide(DAYS_PER_WEEK, 2, RoundingMode.HALF_UP);
        return direction > 0 ? dailyRate : dailyRate.negate();
    }
}
