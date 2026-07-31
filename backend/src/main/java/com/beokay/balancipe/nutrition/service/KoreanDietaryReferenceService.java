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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KoreanDietaryReferenceService {

    private static final int DEFAULT_REFERENCE_YEAR = 2025;

    private final KoreanDietaryReferenceRepository koreanDietaryReferenceRepository;
    private final AgeGroupResolver ageGroupResolver;

    public List<NutritionReferenceItemResponse> getReference(Gender gender, int ageYears, Integer ageInMonths,
                                                               PregnancyStatus pregnancyStatus, Integer referenceYear) {
        PregnancyStatus resolvedPregnancyStatus = pregnancyStatus != null ? pregnancyStatus : PregnancyStatus.NONE;
        validatePregnancyStatus(gender, resolvedPregnancyStatus);

        int year = referenceYear != null ? referenceYear : DEFAULT_REFERENCE_YEAR;
        AgeGroup ageGroup = ageGroupResolver.resolve(ageYears, ageInMonths);
        List<PregnancyStatus> statuses = resolvedPregnancyStatus == PregnancyStatus.NONE
            ? List.of(PregnancyStatus.NONE)
            : List.of(PregnancyStatus.NONE, resolvedPregnancyStatus);

        List<KoreanDietaryReference> rows = koreanDietaryReferenceRepository
            .findByReferenceYearAndGenderAndAgeGroupAndPregnancyStatusIn(year, gender, ageGroup, statuses);

        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.NUTRITION_REFERENCE_NOT_FOUND);
        }

        Map<NutrientIndicatorKey, BigDecimal> summedByNutrientAndIndicator = new LinkedHashMap<>();
        for (KoreanDietaryReference row : rows) {
            NutrientIndicatorKey key = new NutrientIndicatorKey(row.getNutrientCode(), row.getIndicatorType());
            summedByNutrientAndIndicator.merge(key, row.getValue(), BigDecimal::add);
        }

        return summedByNutrientAndIndicator.entrySet().stream()
            .map(entry -> NutritionReferenceItemResponse.of(
                entry.getKey().nutrientCode(), entry.getKey().indicatorType(), entry.getValue()))
            .toList();
    }

    public List<NutritionReferenceAllItemResponse> getAllReferences(Integer referenceYear) {
        int year = referenceYear != null ? referenceYear : DEFAULT_REFERENCE_YEAR;
        List<KoreanDietaryReference> rows = koreanDietaryReferenceRepository.findByReferenceYear(year);

        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.NUTRITION_REFERENCE_NOT_FOUND);
        }

        return rows.stream()
            .map(NutritionReferenceAllItemResponse::from)
            .toList();
    }

    private void validatePregnancyStatus(Gender gender, PregnancyStatus pregnancyStatus) {
        if (gender != Gender.FEMALE && pregnancyStatus != PregnancyStatus.NONE) {
            throw new BusinessException(ErrorCode.INVALID_PREGNANCY_STATUS);
        }
    }

    private record NutrientIndicatorKey(NutrientCode nutrientCode, IndicatorType indicatorType) {
    }
}
