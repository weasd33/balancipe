package com.beokay.balancipe.nutrition.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgeGroupResolverTest {

    private final AgeGroupResolver ageGroupResolver = new AgeGroupResolver();

    @Test
    void 만_나이_0세_5개월_이하는_영아_0_5개월_구간이다() {
        assertThat(ageGroupResolver.resolve(0, 5)).isEqualTo(AgeGroup.INFANT_0_5M);
    }

    @Test
    void 만_나이_0세_6개월_이상은_영아_6_11개월_구간이다() {
        assertThat(ageGroupResolver.resolve(0, 6)).isEqualTo(AgeGroup.INFANT_6_11M);
    }

    @Test
    void 만_나이_0세에서_개월수가_없으면_영아_0_5개월_구간으로_취급한다() {
        assertThat(ageGroupResolver.resolve(0, null)).isEqualTo(AgeGroup.INFANT_0_5M);
    }

    @Test
    void 연령대_경계값을_정확히_구분한다() {
        assertThat(ageGroupResolver.resolve(2, null)).isEqualTo(AgeGroup.TODDLER_1_2Y);
        assertThat(ageGroupResolver.resolve(3, null)).isEqualTo(AgeGroup.TODDLER_3_5Y);
        assertThat(ageGroupResolver.resolve(5, null)).isEqualTo(AgeGroup.TODDLER_3_5Y);
        assertThat(ageGroupResolver.resolve(6, null)).isEqualTo(AgeGroup.CHILD_6_8Y);
        assertThat(ageGroupResolver.resolve(8, null)).isEqualTo(AgeGroup.CHILD_6_8Y);
        assertThat(ageGroupResolver.resolve(9, null)).isEqualTo(AgeGroup.CHILD_9_11Y);
        assertThat(ageGroupResolver.resolve(11, null)).isEqualTo(AgeGroup.CHILD_9_11Y);
        assertThat(ageGroupResolver.resolve(12, null)).isEqualTo(AgeGroup.ADOLESCENT_12_14Y);
        assertThat(ageGroupResolver.resolve(14, null)).isEqualTo(AgeGroup.ADOLESCENT_12_14Y);
        assertThat(ageGroupResolver.resolve(15, null)).isEqualTo(AgeGroup.ADOLESCENT_15_18Y);
        assertThat(ageGroupResolver.resolve(18, null)).isEqualTo(AgeGroup.ADOLESCENT_15_18Y);
        assertThat(ageGroupResolver.resolve(19, null)).isEqualTo(AgeGroup.ADULT_19_29Y);
        assertThat(ageGroupResolver.resolve(29, null)).isEqualTo(AgeGroup.ADULT_19_29Y);
        assertThat(ageGroupResolver.resolve(30, null)).isEqualTo(AgeGroup.ADULT_30_49Y);
        assertThat(ageGroupResolver.resolve(49, null)).isEqualTo(AgeGroup.ADULT_30_49Y);
        assertThat(ageGroupResolver.resolve(50, null)).isEqualTo(AgeGroup.ADULT_50_64Y);
        assertThat(ageGroupResolver.resolve(64, null)).isEqualTo(AgeGroup.ADULT_50_64Y);
        assertThat(ageGroupResolver.resolve(65, null)).isEqualTo(AgeGroup.SENIOR_65_74Y);
        assertThat(ageGroupResolver.resolve(74, null)).isEqualTo(AgeGroup.SENIOR_65_74Y);
        assertThat(ageGroupResolver.resolve(75, null)).isEqualTo(AgeGroup.SENIOR_75_PLUS);
        assertThat(ageGroupResolver.resolve(100, null)).isEqualTo(AgeGroup.SENIOR_75_PLUS);
    }
}
