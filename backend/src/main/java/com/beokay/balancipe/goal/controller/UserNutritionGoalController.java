package com.beokay.balancipe.goal.controller;

import com.beokay.balancipe.global.common.ApiResponse;
import com.beokay.balancipe.global.security.CustomUserDetails;
import com.beokay.balancipe.goal.dto.NutritionGoalResponse;
import com.beokay.balancipe.goal.dto.NutritionGoalUpdateRequest;
import com.beokay.balancipe.goal.service.UserNutritionGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
