package com.beokay.balancipe.nutrition.repository;

import com.beokay.balancipe.nutrition.domain.AgeGroup;
import com.beokay.balancipe.nutrition.domain.KoreanDietaryReference;
import com.beokay.balancipe.nutrition.domain.PregnancyStatus;
import com.beokay.balancipe.user.domain.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KoreanDietaryReferenceRepository extends JpaRepository<KoreanDietaryReference, Long> {

    List<KoreanDietaryReference> findByReferenceYearAndGenderAndAgeGroupAndPregnancyStatusIn(
        int referenceYear, Gender gender, AgeGroup ageGroup, List<PregnancyStatus> pregnancyStatuses);

    List<KoreanDietaryReference> findByReferenceYear(int referenceYear);
}
