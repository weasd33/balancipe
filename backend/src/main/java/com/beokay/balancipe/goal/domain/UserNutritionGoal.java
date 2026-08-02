package com.beokay.balancipe.goal.domain;

import com.beokay.balancipe.global.common.BaseEntity;
import com.beokay.balancipe.nutrition.domain.PregnancyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
    name = "user_nutrition_goal",
    uniqueConstraints = @UniqueConstraint(name = "uk_user_nutrition_goal_user_id", columnNames = "user_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNutritionGoal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 관계 매핑 없이 순수 FK 컬럼 (User 애그리거트와 분리, 기존 코드 스타일과 일치)
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pregnancy_status", nullable = false, length = 30)
    private PregnancyStatus pregnancyStatus;

    // 식이섬유 목표 산출에 사용한 KDRI 버전 (추적용)
    @Column(name = "reference_year", nullable = false)
    private int referenceYear;

    // 기본값 KDRI EER, 사용자가 직접 수정 가능
    @Column(name = "target_calorie", nullable = false, precision = 10, scale = 2)
    private BigDecimal targetCalorie;

    @Enumerated(EnumType.STRING)
    @Column(name = "macro_calculation_method", nullable = false, length = 30)
    private MacroCalculationMethod macroCalculationMethod;

    // method == RATIO_PRESET일 때만 사용
    @Enumerated(EnumType.STRING)
    @Column(name = "macro_preset_type", length = 30)
    private MacroPresetType macroPresetType;

    @Column(name = "height_cm", precision = 6, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "current_weight_kg", precision = 6, scale = 2)
    private BigDecimal currentWeightKg;

    // 목표 칼로리 자동 제안 + method == WEIGHT_BASED_FORMULA(매크로) 둘 다 사용
    @Column(name = "target_weight_kg", precision = 6, scale = 2)
    private BigDecimal targetWeightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", length = 20)
    private ActivityLevel activityLevel;

    @Column(name = "weekly_rate_kg", precision = 4, scale = 2)
    private BigDecimal weeklyRateKg;

    // method == WEIGHT_BASED_FORMULA일 때 사용 (g/kg)
    @Column(name = "protein_per_kg", precision = 5, scale = 2)
    private BigDecimal proteinPerKg;

    @Column(name = "fat_per_kg", precision = 5, scale = 2)
    private BigDecimal fatPerKg;

    // 아래 4개는 입력값 + NutritionPresetCalculator로 계산된 "최종 확정 목표치" (조회 시 재계산 없이 바로 응답)
    @Column(name = "target_carbohydrate", nullable = false, precision = 10, scale = 2)
    private BigDecimal targetCarbohydrate;

    @Column(name = "target_protein", nullable = false, precision = 10, scale = 2)
    private BigDecimal targetProtein;

    @Column(name = "target_fat", nullable = false, precision = 10, scale = 2)
    private BigDecimal targetFat;

    // KDRI RDA/AI에서 그대로 가져옴 (매크로 계산과 무관, 사용자 수정 대상 아님)
    @Column(name = "target_dietary_fiber", nullable = false, precision = 10, scale = 2)
    private BigDecimal targetDietaryFiber;

    @Builder
    private UserNutritionGoal(Long userId, PregnancyStatus pregnancyStatus, int referenceYear,
                               BigDecimal targetCalorie, MacroCalculationMethod macroCalculationMethod,
                               MacroPresetType macroPresetType, BigDecimal heightCm, BigDecimal currentWeightKg,
                               BigDecimal targetWeightKg, ActivityLevel activityLevel, BigDecimal weeklyRateKg,
                               BigDecimal proteinPerKg, BigDecimal fatPerKg, BigDecimal targetCarbohydrate,
                               BigDecimal targetProtein, BigDecimal targetFat, BigDecimal targetDietaryFiber) {
        this.userId = userId;
        this.pregnancyStatus = pregnancyStatus;
        this.referenceYear = referenceYear;
        this.targetCalorie = targetCalorie;
        this.macroCalculationMethod = macroCalculationMethod;
        this.macroPresetType = macroPresetType;
        this.heightCm = heightCm;
        this.currentWeightKg = currentWeightKg;
        this.targetWeightKg = targetWeightKg;
        this.activityLevel = activityLevel;
        this.weeklyRateKg = weeklyRateKg;
        this.proteinPerKg = proteinPerKg;
        this.fatPerKg = fatPerKg;
        this.targetCarbohydrate = targetCarbohydrate;
        this.targetProtein = targetProtein;
        this.targetFat = targetFat;
        this.targetDietaryFiber = targetDietaryFiber;
    }

    public void update(PregnancyStatus pregnancyStatus, int referenceYear,
                        BigDecimal targetCalorie, MacroCalculationMethod macroCalculationMethod,
                        MacroPresetType macroPresetType, BigDecimal heightCm, BigDecimal currentWeightKg,
                        BigDecimal targetWeightKg, ActivityLevel activityLevel, BigDecimal weeklyRateKg,
                        BigDecimal proteinPerKg, BigDecimal fatPerKg, BigDecimal targetCarbohydrate,
                        BigDecimal targetProtein, BigDecimal targetFat, BigDecimal targetDietaryFiber) {
        this.pregnancyStatus = pregnancyStatus;
        this.referenceYear = referenceYear;
        this.targetCalorie = targetCalorie;
        this.macroCalculationMethod = macroCalculationMethod;
        this.macroPresetType = macroPresetType;
        this.heightCm = heightCm;
        this.currentWeightKg = currentWeightKg;
        this.targetWeightKg = targetWeightKg;
        this.activityLevel = activityLevel;
        this.weeklyRateKg = weeklyRateKg;
        this.proteinPerKg = proteinPerKg;
        this.fatPerKg = fatPerKg;
        this.targetCarbohydrate = targetCarbohydrate;
        this.targetProtein = targetProtein;
        this.targetFat = targetFat;
        this.targetDietaryFiber = targetDietaryFiber;
    }

    public static UserNutritionGoal create(Long userId, PregnancyStatus pregnancyStatus, int referenceYear,
                                            BigDecimal targetCalorie, MacroCalculationMethod macroCalculationMethod,
                                            MacroPresetType macroPresetType, BigDecimal heightCm, BigDecimal currentWeightKg,
                                            BigDecimal targetWeightKg, ActivityLevel activityLevel, BigDecimal weeklyRateKg,
                                            BigDecimal proteinPerKg, BigDecimal fatPerKg, BigDecimal targetCarbohydrate,
                                            BigDecimal targetProtein, BigDecimal targetFat, BigDecimal targetDietaryFiber) {
        return UserNutritionGoal.builder()
            .userId(userId)
            .pregnancyStatus(pregnancyStatus)
            .referenceYear(referenceYear)
            .targetCalorie(targetCalorie)
            .macroCalculationMethod(macroCalculationMethod)
            .macroPresetType(macroPresetType)
            .heightCm(heightCm)
            .currentWeightKg(currentWeightKg)
            .targetWeightKg(targetWeightKg)
            .activityLevel(activityLevel)
            .weeklyRateKg(weeklyRateKg)
            .proteinPerKg(proteinPerKg)
            .fatPerKg(fatPerKg)
            .targetCarbohydrate(targetCarbohydrate)
            .targetProtein(targetProtein)
            .targetFat(targetFat)
            .targetDietaryFiber(targetDietaryFiber)
            .build();
    }
}
