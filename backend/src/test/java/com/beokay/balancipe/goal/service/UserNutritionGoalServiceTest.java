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
import com.beokay.balancipe.goal.fixture.NutritionGoalUpdateRequestFixture;
import com.beokay.balancipe.goal.repository.UserNutritionGoalRepository;
import com.beokay.balancipe.nutrition.domain.NutrientCode;
import com.beokay.balancipe.nutrition.domain.PregnancyStatus;
import com.beokay.balancipe.nutrition.dto.NutritionReferenceItemResponse;
import com.beokay.balancipe.nutrition.service.KoreanDietaryReferenceService;
import com.beokay.balancipe.user.domain.Gender;
import com.beokay.balancipe.user.domain.User;
import com.beokay.balancipe.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserNutritionGoalServiceTest {

    @Mock
    private UserNutritionGoalRepository userNutritionGoalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KoreanDietaryReferenceService koreanDietaryReferenceService;

    @Mock
    private NutritionPresetCalculator nutritionPresetCalculator;

    @Mock
    private CalorieGoalCalculator calorieGoalCalculator;

    @InjectMocks
    private UserNutritionGoalService userNutritionGoalService;

    @Test
    void 목표_칼로리를_저장_없이_제안한다() {
        User user = femaleUser();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(calorieGoalCalculator.calculate(
                eq(Gender.FEMALE), anyInt(), eq(BigDecimal.valueOf(165)), eq(BigDecimal.valueOf(60)),
                eq(BigDecimal.valueOf(55)), eq(ActivityLevel.MODERATE), eq(BigDecimal.valueOf(0.5))))
                .willReturn(BigDecimal.valueOf(1496.39));

        SuggestedCalorieResponse response = userNutritionGoalService.suggestCalorie(
                1L, BigDecimal.valueOf(165), BigDecimal.valueOf(60), BigDecimal.valueOf(55),
                ActivityLevel.MODERATE, BigDecimal.valueOf(0.5));

        assertThat(response.suggestedCalorie()).isEqualByComparingTo(BigDecimal.valueOf(1496.39));
    }

    @Test
    void 설정된_영양_목표를_조회한다() {
        UserNutritionGoal goal = UserNutritionGoal.create(
                1L, PregnancyStatus.NONE, 2025,
                BigDecimal.valueOf(2000), MacroCalculationMethod.RATIO_PRESET, MacroPresetType.KETOGENIC,
                BigDecimal.valueOf(170), BigDecimal.valueOf(70), BigDecimal.valueOf(65),
                ActivityLevel.MODERATE, BigDecimal.valueOf(0.5), null, null,
                BigDecimal.valueOf(50), BigDecimal.valueOf(150), BigDecimal.valueOf(133.3), BigDecimal.valueOf(25));
        given(userNutritionGoalRepository.findByUserId(1L)).willReturn(Optional.of(goal));

        NutritionGoalResponse response = userNutritionGoalService.getMyGoal(1L);

        assertThat(response.targetCalorie()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(response.macroCalculationMethod()).isEqualTo(MacroCalculationMethod.RATIO_PRESET);
        assertThat(response.macroPresetType()).isEqualTo(MacroPresetType.KETOGENIC);
        assertThat(response.targetProtein()).isEqualByComparingTo(BigDecimal.valueOf(150));
    }

    @Test
    void 목표가_설정되지_않았으면_예외를_던진다() {
        given(userNutritionGoalRepository.findByUserId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userNutritionGoalService.getMyGoal(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NUTRITION_GOAL_NOT_FOUND);
    }

    @Test
    void 신규_영양_목표를_설정한다() {
        User user = femaleUser();
        NutritionGoalUpdateRequest request = NutritionGoalUpdateRequestFixture.VALID_RATIO_PRESET_REQUEST.getRequest();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userNutritionGoalRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(koreanDietaryReferenceService.getReference(
                eq(Gender.FEMALE), anyInt(), isNull(), eq(PregnancyStatus.NONE), eq(2025)))
                .willReturn(List.of(NutritionReferenceItemResponse.of(NutrientCode.DIETARY_FIBER, null, BigDecimal.valueOf(25))));
        given(nutritionPresetCalculator.calculateByRatio(BigDecimal.valueOf(2000), 10, 30, 60))
                .willReturn(new NutritionPresetCalculator.MacroResult(
                        BigDecimal.valueOf(50), BigDecimal.valueOf(150), BigDecimal.valueOf(133.3)));

        NutritionGoalResponse response = userNutritionGoalService.updateMyGoal(1L, request);

        assertThat(response.targetCalorie()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(response.targetDietaryFiber()).isEqualByComparingTo(BigDecimal.valueOf(25));
        assertThat(response.targetProtein()).isEqualByComparingTo(BigDecimal.valueOf(150));
    }

    @Test
    void 기존_영양_목표를_수정한다() {
        User user = femaleUser();
        UserNutritionGoal existingGoal = UserNutritionGoal.create(
                1L, PregnancyStatus.NONE, 2025,
                BigDecimal.valueOf(1800), MacroCalculationMethod.RATIO_PRESET, MacroPresetType.LOW_FAT_HIGH_CARB,
                BigDecimal.valueOf(160), BigDecimal.valueOf(60), BigDecimal.valueOf(58),
                ActivityLevel.LIGHT, BigDecimal.valueOf(0.3), null, null,
                BigDecimal.valueOf(60), BigDecimal.valueOf(90), BigDecimal.valueOf(40), BigDecimal.valueOf(20));
        NutritionGoalUpdateRequest request = NutritionGoalUpdateRequestFixture.VALID_RATIO_PRESET_REQUEST.getRequest();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userNutritionGoalRepository.findByUserId(1L)).willReturn(Optional.of(existingGoal));
        given(koreanDietaryReferenceService.getReference(
                eq(Gender.FEMALE), anyInt(), isNull(), eq(PregnancyStatus.NONE), eq(2025)))
                .willReturn(List.of(NutritionReferenceItemResponse.of(NutrientCode.DIETARY_FIBER, null, BigDecimal.valueOf(25))));
        given(nutritionPresetCalculator.calculateByRatio(BigDecimal.valueOf(2000), 10, 30, 60))
                .willReturn(new NutritionPresetCalculator.MacroResult(
                        BigDecimal.valueOf(50), BigDecimal.valueOf(150), BigDecimal.valueOf(133.3)));

        NutritionGoalResponse response = userNutritionGoalService.updateMyGoal(1L, request);

        assertThat(response.targetCalorie()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(response.macroPresetType()).isEqualTo(MacroPresetType.KETOGENIC);
    }

    @Test
    void 남성이_임신_상태로_목표를_설정하면_예외를_던진다() {
        User user = maleUser();
        NutritionGoalUpdateRequest base = NutritionGoalUpdateRequestFixture.VALID_RATIO_PRESET_REQUEST.getRequest();
        NutritionGoalUpdateRequest request = new NutritionGoalUpdateRequest(
                PregnancyStatus.PREGNANT_TRIMESTER_1, base.heightCm(), base.currentWeightKg(), base.targetWeightKg(),
                base.activityLevel(), base.weeklyRateKg(), base.targetCalorie(),
                base.macroCalculationMethod(), base.macroPresetType(),
                null, null, null, null, null);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> userNutritionGoalService.updateMyGoal(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PREGNANCY_STATUS);
    }

    @Test
    void 체중_기반_방식으로_목표를_설정한다() {
        User user = femaleUser();
        NutritionGoalUpdateRequest request = NutritionGoalUpdateRequestFixture.VALID_WEIGHT_BASED_REQUEST.getRequest();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userNutritionGoalRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(koreanDietaryReferenceService.getReference(
                eq(Gender.FEMALE), anyInt(), isNull(), eq(PregnancyStatus.NONE), eq(2025)))
                .willReturn(List.of(NutritionReferenceItemResponse.of(NutrientCode.DIETARY_FIBER, null, BigDecimal.valueOf(25))));
        given(nutritionPresetCalculator.calculateByWeight(
                BigDecimal.valueOf(2000), BigDecimal.valueOf(70), BigDecimal.valueOf(1.8), BigDecimal.valueOf(0.8)))
                .willReturn(new NutritionPresetCalculator.MacroResult(
                        BigDecimal.valueOf(248), BigDecimal.valueOf(126), BigDecimal.valueOf(56)));

        NutritionGoalResponse response = userNutritionGoalService.updateMyGoal(1L, request);

        assertThat(response.proteinPerKg()).isEqualByComparingTo(BigDecimal.valueOf(1.8));
        assertThat(response.targetProtein()).isEqualByComparingTo(BigDecimal.valueOf(126));
    }

    @Test
    void RATIO_PRESET인데_프리셋_타입이_없으면_예외를_던진다() {
        User user = femaleUser();
        NutritionGoalUpdateRequest base = NutritionGoalUpdateRequestFixture.VALID_RATIO_PRESET_REQUEST.getRequest();
        NutritionGoalUpdateRequest request = new NutritionGoalUpdateRequest(
                base.pregnancyStatus(), base.heightCm(), base.currentWeightKg(), base.targetWeightKg(),
                base.activityLevel(), base.weeklyRateKg(), base.targetCalorie(),
                MacroCalculationMethod.RATIO_PRESET, null,
                null, null, null, null, null);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(koreanDietaryReferenceService.getReference(any(), anyInt(), isNull(), any(), anyInt()))
                .willReturn(List.of(NutritionReferenceItemResponse.of(NutrientCode.DIETARY_FIBER, null, BigDecimal.valueOf(25))));

        assertThatThrownBy(() -> userNutritionGoalService.updateMyGoal(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_NUTRITION_GOAL);
    }

    private User femaleUser() {
        User user = User.create("hello@gmail.com", "encoded-password", "짱구", Gender.FEMALE, 1996);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private User maleUser() {
        User user = User.create("hello@gmail.com", "encoded-password", "훈이", Gender.MALE, 1996);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }
}
