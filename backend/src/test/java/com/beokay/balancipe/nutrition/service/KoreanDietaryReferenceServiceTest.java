package com.beokay.balancipe.nutrition.service;

import com.beokay.balancipe.global.exception.BusinessException;
import com.beokay.balancipe.global.exception.ErrorCode;
import com.beokay.balancipe.nutrition.domain.AgeGroup;
import com.beokay.balancipe.nutrition.domain.AgeGroupResolver;
import com.beokay.balancipe.nutrition.domain.IndicatorType;
import com.beokay.balancipe.nutrition.domain.KoreanDietaryReference;
import com.beokay.balancipe.nutrition.domain.NutrientCode;
import com.beokay.balancipe.nutrition.domain.PregnancyStatus;
import com.beokay.balancipe.nutrition.dto.NutritionReferenceAllItemResponse;
import com.beokay.balancipe.nutrition.dto.NutritionReferenceItemResponse;
import com.beokay.balancipe.nutrition.repository.KoreanDietaryReferenceRepository;
import com.beokay.balancipe.user.domain.Gender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KoreanDietaryReferenceServiceTest {

    @Mock
    private KoreanDietaryReferenceRepository koreanDietaryReferenceRepository;

    @Mock
    private AgeGroupResolver ageGroupResolver;

    @InjectMocks
    private KoreanDietaryReferenceService koreanDietaryReferenceService;

    @Test
    void 기준값만_존재하면_그대로_반환한다() {
        given(ageGroupResolver.resolve(32, null)).willReturn(AgeGroup.ADULT_30_49Y);
        given(koreanDietaryReferenceRepository.findByReferenceYearAndGenderAndAgeGroupAndPregnancyStatusIn(
            2025, Gender.FEMALE, AgeGroup.ADULT_30_49Y, List.of(PregnancyStatus.NONE)))
            .willReturn(List.of(referenceRow(Gender.FEMALE, PregnancyStatus.NONE, NutrientCode.CALCIUM, IndicatorType.RDA, "700.000")));

        List<NutritionReferenceItemResponse> response =
            koreanDietaryReferenceService.getReference(Gender.FEMALE, 32, null, null, null);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).nutrientCode()).isEqualTo(NutrientCode.CALCIUM);
        assertThat(response.get(0).value()).isEqualByComparingTo("700.000");
    }

    @Test
    void 임신부_가산값을_기준값에_합산한다() {
        given(ageGroupResolver.resolve(32, null)).willReturn(AgeGroup.ADULT_30_49Y);
        given(koreanDietaryReferenceRepository.findByReferenceYearAndGenderAndAgeGroupAndPregnancyStatusIn(
            2025, Gender.FEMALE, AgeGroup.ADULT_30_49Y, List.of(PregnancyStatus.NONE, PregnancyStatus.PREGNANT_TRIMESTER_2)))
            .willReturn(List.of(
                referenceRow(Gender.FEMALE, PregnancyStatus.NONE, NutrientCode.IRON, IndicatorType.RDA, "14.000"),
                referenceRow(Gender.FEMALE, PregnancyStatus.PREGNANT_TRIMESTER_2, NutrientCode.IRON, IndicatorType.RDA, "10.000")
            ));

        List<NutritionReferenceItemResponse> response = koreanDietaryReferenceService.getReference(
            Gender.FEMALE, 32, null, PregnancyStatus.PREGNANT_TRIMESTER_2, null);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).value()).isEqualByComparingTo("24.000");
    }

    @Test
    void 남성에게_임신상태를_지정하면_예외를_던진다() {
        assertThatThrownBy(() -> koreanDietaryReferenceService.getReference(
            Gender.MALE, 32, null, PregnancyStatus.PREGNANT_TRIMESTER_1, null))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_PREGNANCY_STATUS);
    }

    @Test
    void 조건에_해당하는_데이터가_없으면_예외를_던진다() {
        given(ageGroupResolver.resolve(anyInt(), any())).willReturn(AgeGroup.ADULT_19_29Y);
        given(koreanDietaryReferenceRepository.findByReferenceYearAndGenderAndAgeGroupAndPregnancyStatusIn(
            anyInt(), any(), any(), any()))
            .willReturn(List.of());

        assertThatThrownBy(() -> koreanDietaryReferenceService.getReference(Gender.MALE, 25, null, null, null))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NUTRITION_REFERENCE_NOT_FOUND);
    }

    @Test
    void 전체_연령대_기준값을_조회한다() {
        given(koreanDietaryReferenceRepository.findByReferenceYear(2025))
            .willReturn(List.of(referenceRow(Gender.MALE, PregnancyStatus.NONE, NutrientCode.ENERGY, IndicatorType.EAR, "2600.000")));

        List<NutritionReferenceAllItemResponse> response = koreanDietaryReferenceService.getAllReferences(null);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).nutrientCode()).isEqualTo(NutrientCode.ENERGY);
    }

    @Test
    void 전체_연령대_조회시_데이터가_없으면_예외를_던진다() {
        given(koreanDietaryReferenceRepository.findByReferenceYear(2020)).willReturn(List.of());

        assertThatThrownBy(() -> koreanDietaryReferenceService.getAllReferences(2020))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NUTRITION_REFERENCE_NOT_FOUND);
    }

    private KoreanDietaryReference referenceRow(Gender gender, PregnancyStatus pregnancyStatus,
                                                 NutrientCode nutrientCode, IndicatorType indicatorType, String value) {
        return KoreanDietaryReference.create(
            2025, gender, AgeGroup.ADULT_30_49Y, pregnancyStatus, nutrientCode, indicatorType, new BigDecimal(value));
    }
}
