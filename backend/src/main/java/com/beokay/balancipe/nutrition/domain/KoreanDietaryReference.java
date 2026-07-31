package com.beokay.balancipe.nutrition.domain;

import com.beokay.balancipe.global.common.BaseEntity;
import com.beokay.balancipe.user.domain.Gender;
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
    name = "korean_dietary_reference",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_korean_dietary_reference",
        columnNames = {"reference_year", "gender", "age_group", "pregnancy_status", "nutrient_code", "indicator_type"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KoreanDietaryReference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_year", nullable = false)
    private int referenceYear; // 2020 또는 2025 (향후 개정판 확장 가능)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender; // user.domain.Gender 재사용, 영유아 구간은 남녀 행에 동일 값 중복 저장

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false, length = 30)
    private AgeGroup ageGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "pregnancy_status", nullable = false, length = 30)
    private PregnancyStatus pregnancyStatus; // NONE이 기준값, 그 외는 기준값 대비 가산값

    @Enumerated(EnumType.STRING)
    @Column(name = "nutrient_code", nullable = false, length = 30)
    private NutrientCode nutrientCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "indicator_type", nullable = false, length = 10)
    private IndicatorType indicatorType;

    // 컬럼명 value는 H2 등 일부 DB에서 예약어라 reference_value로 매핑 (V1의 "users" 테이블명 회피와 동일한 이유)
    @Column(name = "reference_value", nullable = false, precision = 10, scale = 3)
    private BigDecimal value; // pregnancyStatus != NONE인 행은 가산값만 저장

    @Builder
    private KoreanDietaryReference(int referenceYear, Gender gender, AgeGroup ageGroup,
                                    PregnancyStatus pregnancyStatus, NutrientCode nutrientCode,
                                    IndicatorType indicatorType, BigDecimal value) {
        this.referenceYear = referenceYear;
        this.gender = gender;
        this.ageGroup = ageGroup;
        this.pregnancyStatus = pregnancyStatus;
        this.nutrientCode = nutrientCode;
        this.indicatorType = indicatorType;
        this.value = value;
    }

    public static KoreanDietaryReference create(int referenceYear, Gender gender, AgeGroup ageGroup,
                                                  PregnancyStatus pregnancyStatus, NutrientCode nutrientCode,
                                                  IndicatorType indicatorType, BigDecimal value) {
        return KoreanDietaryReference.builder()
            .referenceYear(referenceYear)
            .gender(gender)
            .ageGroup(ageGroup)
            .pregnancyStatus(pregnancyStatus)
            .nutrientCode(nutrientCode)
            .indicatorType(indicatorType)
            .value(value)
            .build();
    }
}
