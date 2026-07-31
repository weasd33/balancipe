package com.beokay.balancipe.nutrition.domain;

public enum IndicatorType {
    EAR,  // 평균필요량(Estimated Average Requirement)
    RDA,  // 권장섭취량(Recommended Dietary Allowance)
    AI,   // 충분섭취량(Adequate Intake, EAR/RDA 산출 불가 시)
    UL    // 상한섭취량(Tolerable Upper Intake Level, 과잉 위험 있는 영양소만 존재)
}
