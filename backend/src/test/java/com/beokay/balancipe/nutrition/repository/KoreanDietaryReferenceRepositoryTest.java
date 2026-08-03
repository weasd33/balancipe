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

    // V3/V4 마이그레이션이 이미 (2020/2025, FEMALE, ADULT_30_49Y, IRON) 조합을 시딩해두므로
    // 별도 fixture insert 없이 실제 시딩 데이터로 검증(중복 insert 시 유니크 제약 위반)
    @Test
    void 연도_성별_연령대_임신상태_조건으로_조회한다() {
        List<KoreanDietaryReference> rows = koreanDietaryReferenceRepository
            .findByReferenceYearAndGenderAndAgeGroupAndPregnancyStatusIn(
                2025, Gender.FEMALE, AgeGroup.ADULT_30_49Y,
                List.of(PregnancyStatus.NONE, PregnancyStatus.PREGNANT_TRIMESTER_2));

        assertThat(rows).extracting(KoreanDietaryReference::getReferenceYear).containsOnly(2025);
        assertThat(rows).filteredOn(row -> row.getNutrientCode() == NutrientCode.IRON
                && row.getIndicatorType() == IndicatorType.RDA)
            .extracting(KoreanDietaryReference::getPregnancyStatus, KoreanDietaryReference::getValue)
            .containsExactlyInAnyOrder(
                org.assertj.core.groups.Tuple.tuple(PregnancyStatus.NONE, new BigDecimal("12.000")),
                org.assertj.core.groups.Tuple.tuple(PregnancyStatus.PREGNANT_TRIMESTER_2, new BigDecimal("9.000")));
    }

    @Test
    void 조건에_맞는_데이터가_없으면_빈_목록을_반환한다() {
        List<KoreanDietaryReference> rows = koreanDietaryReferenceRepository
            .findByReferenceYearAndGenderAndAgeGroupAndPregnancyStatusIn(
                1999, Gender.MALE, AgeGroup.SENIOR_75_PLUS, List.of(PregnancyStatus.NONE));

        assertThat(rows).isEmpty();
    }

    @Test
    void 연도로_전체_행을_조회한다() {
        koreanDietaryReferenceRepository.save(KoreanDietaryReference.create(
            1999, Gender.MALE, AgeGroup.ADULT_19_29Y, PregnancyStatus.NONE,
            NutrientCode.ENERGY, IndicatorType.EAR, new BigDecimal("2600.000")));
        koreanDietaryReferenceRepository.save(KoreanDietaryReference.create(
            1999, Gender.FEMALE, AgeGroup.ADULT_19_29Y, PregnancyStatus.NONE,
            NutrientCode.ENERGY, IndicatorType.EAR, new BigDecimal("2000.000")));

        List<KoreanDietaryReference> rows = koreanDietaryReferenceRepository.findByReferenceYear(1999);

        assertThat(rows).hasSize(2);
    }
}
