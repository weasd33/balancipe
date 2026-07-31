package com.beokay.balancipe.nutrition.domain;

import org.springframework.stereotype.Component;

/*
    만 나이(년) -> AgeGroup 변환. 영아 구간(0-5개월/6-11개월)은 연 단위만으로 구분할 수 없어
    ageYears == 0일 때만 ageInMonths를 함께 참고
 */
@Component
public class AgeGroupResolver {

    private static final int INFANT_MONTH_BOUNDARY = 5;

    public AgeGroup resolve(int ageYears, Integer ageInMonths) {
        if (ageYears == 0) {
            int months = ageInMonths != null ? ageInMonths : 0;
            return months <= INFANT_MONTH_BOUNDARY ? AgeGroup.INFANT_0_5M : AgeGroup.INFANT_6_11M;
        }
        if (ageYears <= 2) return AgeGroup.TODDLER_1_2Y;
        if (ageYears <= 5) return AgeGroup.TODDLER_3_5Y;
        if (ageYears <= 8) return AgeGroup.CHILD_6_8Y;
        if (ageYears <= 11) return AgeGroup.CHILD_9_11Y;
        if (ageYears <= 14) return AgeGroup.ADOLESCENT_12_14Y;
        if (ageYears <= 18) return AgeGroup.ADOLESCENT_15_18Y;
        if (ageYears <= 29) return AgeGroup.ADULT_19_29Y;
        if (ageYears <= 49) return AgeGroup.ADULT_30_49Y;
        if (ageYears <= 64) return AgeGroup.ADULT_50_64Y;
        if (ageYears <= 74) return AgeGroup.SENIOR_65_74Y;
        return AgeGroup.SENIOR_75_PLUS;
    }
}
