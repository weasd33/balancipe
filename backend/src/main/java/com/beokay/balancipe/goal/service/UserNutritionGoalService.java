package com.beokay.balancipe.goal.service;

import com.beokay.balancipe.global.exception.BusinessException;
import com.beokay.balancipe.global.exception.ErrorCode;
import com.beokay.balancipe.goal.domain.UserNutritionGoal;
import com.beokay.balancipe.goal.dto.NutritionGoalResponse;
import com.beokay.balancipe.goal.repository.UserNutritionGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserNutritionGoalService {

    private final UserNutritionGoalRepository userNutritionGoalRepository;

    public NutritionGoalResponse getMyGoal(Long userId) {
        UserNutritionGoal goal = userNutritionGoalRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NUTRITION_GOAL_NOT_FOUND));
        return NutritionGoalResponse.from(goal);
    }
}
