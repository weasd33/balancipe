package com.beokay.balancipe.goal.service;

import com.beokay.balancipe.global.exception.BusinessException;
import com.beokay.balancipe.global.exception.ErrorCode;
import com.beokay.balancipe.goal.domain.ActivityLevel;
import com.beokay.balancipe.goal.domain.MacroCalculationMethod;
import com.beokay.balancipe.goal.domain.MacroPresetType;
import com.beokay.balancipe.goal.domain.UserNutritionGoal;
import com.beokay.balancipe.goal.dto.NutritionGoalResponse;
import com.beokay.balancipe.goal.repository.UserNutritionGoalRepository;
import com.beokay.balancipe.nutrition.domain.PregnancyStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserNutritionGoalServiceTest {

    @Mock
    private UserNutritionGoalRepository userNutritionGoalRepository;

    @InjectMocks
    private UserNutritionGoalService userNutritionGoalService;

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
}
