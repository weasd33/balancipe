package com.beokay.balancipe.goal.controller;

import com.beokay.balancipe.global.common.ApiResponse;
import com.beokay.balancipe.global.security.CustomUserDetails;
import com.beokay.balancipe.goal.domain.ActivityLevel;
import com.beokay.balancipe.goal.dto.NutritionGoalResponse;
import com.beokay.balancipe.goal.dto.NutritionGoalUpdateRequest;
import com.beokay.balancipe.goal.dto.SuggestedCalorieResponse;
import com.beokay.balancipe.goal.service.UserNutritionGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Tag(name = "Nutrition Goal", description = "사용자 영양 목표 관련 API")
@RestController
@RequestMapping("/api/users/me/nutrition-goal")
@RequiredArgsConstructor
public class UserNutritionGoalController {

    private final UserNutritionGoalService userNutritionGoalService;

    @Operation(summary = "내 영양 목표 조회", description = "인증된 사용자가 설정한 영양 목표를 조회한다. 미설정 시 404를 반환한다.")
    @GetMapping
    public ResponseEntity<ApiResponse<NutritionGoalResponse>> getMyGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        NutritionGoalResponse response = userNutritionGoalService.getMyGoal(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "내 영양 목표 설정", description = "영양 목표를 새로 설정하거나 기존 설정을 수정한다. 목표 칼로리는 클라이언트가 보낸 값을 그대로 저장한다.")
    @PutMapping
    public ResponseEntity<ApiResponse<NutritionGoalResponse>> updateMyGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody NutritionGoalUpdateRequest request) {
        NutritionGoalResponse response = userNutritionGoalService.updateMyGoal(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "목표 칼로리 자동 제안",
        description = "키/체중/활동량/목표 체중 변화율을 바탕으로 목표 칼로리를 계산해 미리보기로 반환한다. 저장하지 않는다.")
    @GetMapping("/suggested-calorie")
    public ResponseEntity<ApiResponse<SuggestedCalorieResponse>> suggestCalorie(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "키(cm)", example = "170.00") @RequestParam BigDecimal heightCm,
            @Parameter(description = "현재 체중(kg)", example = "70.00") @RequestParam BigDecimal currentWeightKg,
            @Parameter(description = "목표 체중(kg)", example = "65.00") @RequestParam BigDecimal targetWeightKg,
            @Parameter(description = "활동량 수준", example = "MODERATE") @RequestParam ActivityLevel activityLevel,
            @Parameter(description = "주당 목표 체중 변화율(kg/주)", example = "0.50") @RequestParam BigDecimal weeklyRateKg) {
        SuggestedCalorieResponse response = userNutritionGoalService.suggestCalorie(
                userDetails.getUserId(), heightCm, currentWeightKg, targetWeightKg, activityLevel, weeklyRateKg);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
