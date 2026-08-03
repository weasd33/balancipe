package com.beokay.balancipe.goal.service;

import com.beokay.balancipe.global.exception.BusinessException;
import com.beokay.balancipe.global.exception.ErrorCode;
import com.beokay.balancipe.goal.domain.ActivityLevel;
import com.beokay.balancipe.goal.domain.CalorieGoalCalculator;
import com.beokay.balancipe.goal.domain.MacroCalculationMethod;
import com.beokay.balancipe.goal.domain.MacroPresetType;
import com.beokay.balancipe.goal.domain.NutritionPresetCalculator;
import com.beokay.balancipe.goal.domain.UserNutritionGoal;
import com.beokay.balancipe.goal.dto.NutritionGoalResponse;
import com.beokay.balancipe.goal.dto.NutritionGoalUpdateRequest;
import com.beokay.balancipe.goal.dto.SuggestedCalorieResponse;
import com.beokay.balancipe.goal.repository.UserNutritionGoalRepository;
import com.beokay.balancipe.nutrition.domain.NutrientCode;
import com.beokay.balancipe.nutrition.domain.PregnancyStatus;
import com.beokay.balancipe.nutrition.dto.NutritionReferenceItemResponse;
import com.beokay.balancipe.nutrition.service.KoreanDietaryReferenceService;
import com.beokay.balancipe.user.domain.Gender;
import com.beokay.balancipe.user.domain.User;
import com.beokay.balancipe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserNutritionGoalService {

    // 목표 저장 시 항상 최신 KDRI 기준으로 계산 (과거 연도 선택 저장은 스코프 밖)
    private static final int DEFAULT_REFERENCE_YEAR = 2025;

    private final UserNutritionGoalRepository userNutritionGoalRepository;
    private final UserRepository userRepository;
    private final KoreanDietaryReferenceService koreanDietaryReferenceService;
    private final NutritionPresetCalculator nutritionPresetCalculator;
    private final CalorieGoalCalculator calorieGoalCalculator;

    // 저장 없는 미리보기: CalorieGoalCalculator로 계산만 하고 반환
    public SuggestedCalorieResponse suggestCalorie(Long userId, BigDecimal heightCm, BigDecimal currentWeightKg,
                                                     BigDecimal targetWeightKg, ActivityLevel activityLevel,
                                                     BigDecimal weeklyRateKg) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        int ageYears = LocalDate.now().getYear() - user.getBirthYear();

        BigDecimal suggestedCalorie = calorieGoalCalculator.calculate(
                user.getGender(), ageYears, heightCm, currentWeightKg, targetWeightKg, activityLevel, weeklyRateKg);
        return SuggestedCalorieResponse.from(suggestedCalorie);
    }

    public NutritionGoalResponse getMyGoal(Long userId) {
        UserNutritionGoal goal = userNutritionGoalRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NUTRITION_GOAL_NOT_FOUND));
        return NutritionGoalResponse.from(goal);
    }

    @Transactional
    public NutritionGoalResponse updateMyGoal(Long userId, NutritionGoalUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        validatePregnancyStatus(user.getGender(), request.pregnancyStatus());

        BigDecimal targetDietaryFiber = resolveTargetDietaryFiber(user, request);
        NutritionPresetCalculator.MacroResult macroResult = calculateMacro(request);
        // method와 무관한 값(macroPresetType/proteinPerKg/fatPerKg)은 엔티티에 null로 저장해 조회 시 혼동 방지
        MacroPresetType macroPresetType = resolveMacroPresetType(request);
        BigDecimal proteinPerKg = resolveProteinPerKg(request);
        BigDecimal fatPerKg = resolveFatPerKg(request);

        // 최초 설정이면 insert, 이미 있으면 update (사용자당 목표는 1개)
        UserNutritionGoal goal = userNutritionGoalRepository.findByUserId(userId).orElse(null);
        if (goal == null) {
            goal = UserNutritionGoal.create(
                    userId, request.pregnancyStatus(), DEFAULT_REFERENCE_YEAR,
                    request.targetCalorie(), request.macroCalculationMethod(), macroPresetType,
                    request.heightCm(), request.currentWeightKg(), request.targetWeightKg(),
                    request.activityLevel(), request.weeklyRateKg(), proteinPerKg, fatPerKg,
                    macroResult.carbohydrate(), macroResult.protein(), macroResult.fat(), targetDietaryFiber);
            userNutritionGoalRepository.save(goal);
        } else {
            goal.update(
                    request.pregnancyStatus(), DEFAULT_REFERENCE_YEAR,
                    request.targetCalorie(), request.macroCalculationMethod(), macroPresetType,
                    request.heightCm(), request.currentWeightKg(), request.targetWeightKg(),
                    request.activityLevel(), request.weeklyRateKg(), proteinPerKg, fatPerKg,
                    macroResult.carbohydrate(), macroResult.protein(), macroResult.fat(), targetDietaryFiber);
        }

        return NutritionGoalResponse.from(goal);
    }

    // 남성인데 임신/수유 상태를 지정하면 잘못된 입력으로 취급
    private void validatePregnancyStatus(Gender gender, PregnancyStatus pregnancyStatus) {
        if (gender != Gender.FEMALE && pregnancyStatus != PregnancyStatus.NONE) {
            throw new BusinessException(ErrorCode.INVALID_PREGNANCY_STATUS);
        }
    }

    // 식이섬유 목표는 매크로 계산과 무관하게 KDRI 기준값을 그대로 가져와 저장
    private BigDecimal resolveTargetDietaryFiber(User user, NutritionGoalUpdateRequest request) {
        int ageYears = LocalDate.now().getYear() - user.getBirthYear();
        List<NutritionReferenceItemResponse> references = koreanDietaryReferenceService.getReference(
                user.getGender(), ageYears, null, request.pregnancyStatus(), DEFAULT_REFERENCE_YEAR);

        return references.stream()
                .filter(reference -> reference.nutrientCode() == NutrientCode.DIETARY_FIBER)
                .map(NutritionReferenceItemResponse::value)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NUTRITION_REFERENCE_NOT_FOUND));
    }

    private NutritionPresetCalculator.MacroResult calculateMacro(NutritionGoalUpdateRequest request) {
        if (request.macroCalculationMethod() == MacroCalculationMethod.RATIO_PRESET) {
            return calculateByRatio(request);
        }
        if (request.macroCalculationMethod() == MacroCalculationMethod.WEIGHT_BASED_FORMULA) {
            return calculateByWeight(request);
        }
        throw new BusinessException(ErrorCode.INVALID_NUTRITION_GOAL);
    }

    private NutritionPresetCalculator.MacroResult calculateByRatio(NutritionGoalUpdateRequest request) {
        MacroPresetType presetType = request.macroPresetType();
        if (presetType == null) {
            throw new BusinessException(ErrorCode.INVALID_NUTRITION_GOAL);
        }

        // CUSTOM은 프리셋에 고정 비율이 없어 사용자가 직접 보낸 비율(합 100 검증은 계산기에서 수행)을 사용
        if (presetType == MacroPresetType.CUSTOM) {
            if (request.customCarbRatio() == null || request.customProteinRatio() == null || request.customFatRatio() == null) {
                throw new BusinessException(ErrorCode.INVALID_NUTRITION_GOAL);
            }
            return nutritionPresetCalculator.calculateByRatio(request.targetCalorie(),
                    request.customCarbRatio(), request.customProteinRatio(), request.customFatRatio());
        }

        return nutritionPresetCalculator.calculateByRatio(request.targetCalorie(),
                presetType.getCarbRatio(), presetType.getProteinRatio(), presetType.getFatRatio());
    }

    private NutritionPresetCalculator.MacroResult calculateByWeight(NutritionGoalUpdateRequest request) {
        if (request.proteinPerKg() == null || request.fatPerKg() == null) {
            throw new BusinessException(ErrorCode.INVALID_NUTRITION_GOAL);
        }
        return nutritionPresetCalculator.calculateByWeight(request.targetCalorie(), request.currentWeightKg(),
                request.proteinPerKg(), request.fatPerKg());
    }

    private MacroPresetType resolveMacroPresetType(NutritionGoalUpdateRequest request) {
        return request.macroCalculationMethod() == MacroCalculationMethod.RATIO_PRESET ? request.macroPresetType() : null;
    }

    private BigDecimal resolveProteinPerKg(NutritionGoalUpdateRequest request) {
        return request.macroCalculationMethod() == MacroCalculationMethod.WEIGHT_BASED_FORMULA ? request.proteinPerKg() : null;
    }

    private BigDecimal resolveFatPerKg(NutritionGoalUpdateRequest request) {
        return request.macroCalculationMethod() == MacroCalculationMethod.WEIGHT_BASED_FORMULA ? request.fatPerKg() : null;
    }
}
