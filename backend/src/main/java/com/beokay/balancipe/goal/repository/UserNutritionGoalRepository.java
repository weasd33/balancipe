package com.beokay.balancipe.goal.repository;

import com.beokay.balancipe.goal.domain.UserNutritionGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserNutritionGoalRepository extends JpaRepository<UserNutritionGoal, Long> {

    Optional<UserNutritionGoal> findByUserId(Long userId);
}
