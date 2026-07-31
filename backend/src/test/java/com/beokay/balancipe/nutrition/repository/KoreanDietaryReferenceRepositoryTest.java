package com.beokay.balancipe.nutrition.repository;

import com.beokay.balancipe.global.config.JpaAuditingConfig;
import com.beokay.balancipe.nutrition.domain.AgeGroup;
import com.beokay.balancipe.nutrition.domain.IndicatorType;
import com.beokay.balancipe.nutrition.domain.KoreanDietaryReference;
import com.beokay.balancipe.nutrition.domain.NutrientCode;
import com.beokay.balancipe.nutrition.domain.PregnancyStatus;
import com.beokay.balancipe.user.domain.Gender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
@Testcontainers
class KoreanDietaryReferenceRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private KoreanDietaryReferenceRepository koreanDietaryReferenceRepository;

    @Test
    void 연도_성별_연령대_임신상태_조건으로_조회한다() {
        koreanDietaryReferenceRepository.save(KoreanDietaryReference.create(
            2025, Gender.FEMALE, AgeGroup.ADULT_30_49Y, PregnancyStatus.NONE,
            NutrientCode.IRON, IndicatorType.RDA, new BigDecimal("14.000")));
        koreanDietaryReferenceRepository.save(KoreanDietaryReference.create(
            2025, Gender.FEMALE, AgeGroup.ADULT_30_49Y, PregnancyStatus.PREGNANT_TRIMESTER_2,
            NutrientCode.IRON, IndicatorType.RDA, new BigDecimal("10.000")));
        koreanDietaryReferenceRepository.save(KoreanDietaryReference.create(
            2020, Gender.FEMALE, AgeGroup.ADULT_30_49Y, PregnancyStatus.NONE,
            NutrientCode.IRON, IndicatorType.RDA, new BigDecimal("12.000")));

        List<KoreanDietaryReference> rows = koreanDietaryReferenceRepository
            .findByReferenceYearAndGenderAndAgeGroupAndPregnancyStatusIn(
                2025, Gender.FEMALE, AgeGroup.ADULT_30_49Y,
                List.of(PregnancyStatus.NONE, PregnancyStatus.PREGNANT_TRIMESTER_2));

        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(KoreanDietaryReference::getReferenceYear).containsOnly(2025);
    }

    @Test
    void 조건에_맞는_데이터가_없으면_빈_목록을_반환한다() {
        List<KoreanDietaryReference> rows = koreanDietaryReferenceRepository
            .findByReferenceYearAndGenderAndAgeGroupAndPregnancyStatusIn(
                2025, Gender.MALE, AgeGroup.SENIOR_75_PLUS, List.of(PregnancyStatus.NONE));

        assertThat(rows).isEmpty();
    }

    @Test
    void 연도로_전체_행을_조회한다() {
        koreanDietaryReferenceRepository.save(KoreanDietaryReference.create(
            2025, Gender.MALE, AgeGroup.ADULT_19_29Y, PregnancyStatus.NONE,
            NutrientCode.ENERGY, IndicatorType.EAR, new BigDecimal("2600.000")));
        koreanDietaryReferenceRepository.save(KoreanDietaryReference.create(
            2025, Gender.FEMALE, AgeGroup.ADULT_19_29Y, PregnancyStatus.NONE,
            NutrientCode.ENERGY, IndicatorType.EAR, new BigDecimal("2000.000")));

        List<KoreanDietaryReference> rows = koreanDietaryReferenceRepository.findByReferenceYear(2025);

        assertThat(rows).hasSize(2);
    }
}
