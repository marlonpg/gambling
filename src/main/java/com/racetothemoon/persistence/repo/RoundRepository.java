package com.racetothemoon.persistence.repo;

import com.racetothemoon.persistence.entity.RoundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoundRepository extends JpaRepository<RoundEntity, Long> {
	Optional<RoundEntity> findTopByOrderByRoundNumberDesc();
}
