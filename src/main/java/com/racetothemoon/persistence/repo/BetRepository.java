package com.racetothemoon.persistence.repo;

import com.racetothemoon.persistence.entity.BetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BetRepository extends JpaRepository<BetEntity, Long> {
    List<BetEntity> findByRound_IdOrderByTargetKmDesc(Long roundId);
}
