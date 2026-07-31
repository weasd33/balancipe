package com.beokay.balancipe.nutrition.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NutrientCode {

    // 다량영양소
    ENERGY("kcal"),          // 에너지
    CARBOHYDRATE("g"),       // 탄수화물
    PROTEIN("g"),            // 단백질
    FAT("g"),                // 지방
    DIETARY_FIBER("g"),      // 식이섬유

    // 비타민
    VITAMIN_A("µg RAE"),     // 비타민 A
    VITAMIN_B1("mg"),        // 비타민 B1(티아민)
    VITAMIN_B2("mg"),        // 비타민 B2(리보플라빈)
    VITAMIN_B3("mg NE"),     // 비타민 B3(니아신)
    VITAMIN_B6("mg"),        // 비타민 B6
    VITAMIN_B9("µg DFE"),    // 비타민 B9(엽산)
    VITAMIN_B12("µg"),       // 비타민 B12
    VITAMIN_C("mg"),         // 비타민 C
    VITAMIN_D("µg"),         // 비타민 D
    VITAMIN_E("mg α-TE"),    // 비타민 E

    // 무기질
    CALCIUM("mg"),           // 칼슘
    SODIUM("mg"),            // 나트륨
    POTASSIUM("mg"),         // 칼륨
    MAGNESIUM("mg"),         // 마그네슘
    IRON("mg");              // 철

    private final String unit;
}
