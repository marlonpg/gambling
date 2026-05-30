package com.racetothemoon.controller;

import com.racetothemoon.model.BetHistoryView;
import com.racetothemoon.model.RoundHistoryView;
import com.racetothemoon.persistence.repo.BetRepository;
import com.racetothemoon.persistence.repo.RoundRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final RoundRepository roundRepository;
    private final BetRepository betRepository;

    public HistoryController(RoundRepository roundRepository, BetRepository betRepository) {
        this.roundRepository = roundRepository;
        this.betRepository = betRepository;
    }

    @GetMapping("/rounds")
    public List<RoundHistoryView> listRounds(@RequestParam(defaultValue = "20") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return roundRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(b.getRoundNumber(), a.getRoundNumber()))
                .limit(safeLimit)
                .map(round -> new RoundHistoryView(
                        round.getId(),
                        round.getRoundNumber(),
                        round.getPhase().name(),
                        round.isMoonMission(),
                        round.isGoldenRocket(),
                        round.getCrashAltitudeKm(),
                        round.getExplodedAtKm(),
                        round.getStartedAt(),
                        round.getFinishedAt()
                ))
                .toList();
    }

    @GetMapping("/rounds/{roundId}/bets")
    public List<BetHistoryView> listRoundBets(@PathVariable Long roundId) {
        return betRepository.findByRound_IdOrderByTargetKmDesc(roundId).stream()
                .map(bet -> new BetHistoryView(
                        bet.getId(),
                        bet.getPlayerName(),
                        bet.getAmount(),
                        bet.getTargetKm(),
                        bet.isCashedOut(),
                        bet.getPayout(),
                        bet.isLost(),
                        bet.getPlacedAt(),
                        bet.getCashedOutAt()
                ))
                .toList();
    }
}
