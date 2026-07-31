package com.beokay.balancipe.nutrition.controller;

import com.beokay.balancipe.global.common.ApiResponse;
import com.beokay.balancipe.nutrition.domain.PregnancyStatus;
import com.beokay.balancipe.nutrition.dto.NutritionReferenceAllItemResponse;
import com.beokay.balancipe.nutrition.dto.NutritionReferenceItemResponse;
import com.beokay.balancipe.nutrition.service.KoreanDietaryReferenceService;
import com.beokay.balancipe.user.domain.Gender;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Nutrition", description = "한국인 영양소 섭취기준 조회 API")
@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final KoreanDietaryReferenceService koreanDietaryReferenceService;

    @Operation(summary = "나이/성별 기반 영양소 섭취기준 조회",
        description = "성별·나이(+임신/수유 상태)에 해당하는 영양소별 섭취기준을 조회한다. 영아(만 0세)는 ageInMonths로 0-5개월/6-11개월을 구분한다.")
    @GetMapping("/reference")
    public ResponseEntity<ApiResponse<List<NutritionReferenceItemResponse>>> getReference(
        @Parameter(description = "성별", example = "FEMALE") @RequestParam Gender gender,
        @Parameter(description = "만 나이(년)", example = "32") @RequestParam int age,
        @Parameter(description = "만 나이가 0세일 때만 사용하는 개월수(0-11)", example = "8")
        @RequestParam(required = false) Integer ageInMonths,
        @Parameter(description = "임신/수유 상태, 기본값 NONE") @RequestParam(required = false) PregnancyStatus pregnancyStatus,
        @Parameter(description = "섭취기준 연도, 기본값 2025") @RequestParam(required = false) Integer referenceYear) {
        List<NutritionReferenceItemResponse> response =
            koreanDietaryReferenceService.getReference(gender, age, ageInMonths, pregnancyStatus, referenceYear);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "전체 연령대 영양소 섭취기준 조회",
        description = "전체 성별×연령대×영양소 조합의 섭취기준을 그리드 형태로 반환한다. 임신부/수유부 부가행도 별도 항목으로 포함된다.")
    @GetMapping("/reference/all")
    public ResponseEntity<ApiResponse<List<NutritionReferenceAllItemResponse>>> getAllReferences(
        @Parameter(description = "섭취기준 연도, 기본값 2025") @RequestParam(required = false) Integer referenceYear) {
        List<NutritionReferenceAllItemResponse> response = koreanDietaryReferenceService.getAllReferences(referenceYear);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
