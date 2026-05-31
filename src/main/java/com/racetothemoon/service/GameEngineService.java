package com.racetothemoon.service;

import com.racetothemoon.model.Bet;
import com.racetothemoon.model.BetResult;
import com.racetothemoon.model.GamePhase;
import com.racetothemoon.model.GameStateSnapshot;
import com.racetothemoon.model.TopTargetView;
import com.racetothemoon.persistence.entity.BetEntity;
import com.racetothemoon.persistence.entity.RoundEntity;
import com.racetothemoon.persistence.repo.BetRepository;
import com.racetothemoon.persistence.repo.RoundRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class GameEngineService {

    private static final int TICK_MS = 100;
    private static final int BETTING_TICKS = 300;
    private static final int EXPLOSION_TICKS = 45;
    private static final int MAX_EVENTS = 8;

    private final SimpMessagingTemplate messagingTemplate;
    private final RoundRepository roundRepository;
    private final BetRepository betRepository;
    private final Random random = new Random();

    private final Map<String, Bet> betsByPlayer = new HashMap<>();
    private final Map<String, Long> betRecordIdByPlayer = new HashMap<>();
    private final ArrayDeque<String> recentEvents = new ArrayDeque<>();

    private long roundId;
    private Long activeRoundRecordId;
    private GamePhase phase = GamePhase.BETTING;
    private int ticksInPhase;
    private double altitudeKm;
    private double speedKmPerSec;
    private long crashAltitudeKm;
    private int flameFrame;
    private boolean moonMission;
    private boolean goldenRocket;
    private boolean soloModeRound;
    private boolean paused;
    private boolean forceNextMoonMission;
    private boolean forceNextGoldenRocket;

    public GameEngineService(
            SimpMessagingTemplate messagingTemplate,
            RoundRepository roundRepository,
            BetRepository betRepository
    ) {
        this.messagingTemplate = messagingTemplate;
        this.roundRepository = roundRepository;
        this.betRepository = betRepository;
    }

    @PostConstruct
    public synchronized void init() {
        roundId = roundRepository.findTopByOrderByRoundNumberDesc()
                .map(RoundEntity::getRoundNumber)
                .orElse(0L);
        startNewRound();
        broadcastSnapshot();
    }

    @Scheduled(fixedRate = TICK_MS)
    public synchronized void tick() {
        if (paused) {
            return;
        }

        flameFrame = (flameFrame + 1) % 3;

        switch (phase) {
            case BETTING -> {
                ticksInPhase++;
                if (ticksInPhase >= BETTING_TICKS) {
                    launch();
                }
            }
            case FLYING -> {
                ticksInPhase++;
                speedKmPerSec += 0.06 + random.nextDouble() * 0.08;
                altitudeKm += speedKmPerSec * (TICK_MS / 1000.0);
                processCashouts();

                if (Math.round(altitudeKm) >= crashAltitudeKm) {
                    explode();
                }
            }
            case EXPLODED -> {
                ticksInPhase++;
                if (ticksInPhase >= EXPLOSION_TICKS) {
                    startNewRound();
                }
            }
        }

        broadcastSnapshot();
    }

    public synchronized BetResult placeBet(String player, long amount, long targetKm) {
        if (phase != GamePhase.BETTING) {
            return new BetResult(false, "Betting is closed for this round.", player, amount, targetKm);
        }
        if (amount <= 0 || targetKm < 20) {
            return new BetResult(false, "Invalid amount or target.", player, amount, targetKm);
        }

        String playerKey = player.toLowerCase();
        Bet bet = new Bet(player, amount, targetKm);
        betsByPlayer.put(playerKey, bet);

        if (activeRoundRecordId != null) {
            BetEntity record = new BetEntity();
            record.setRound(roundRepository.getReferenceById(activeRoundRecordId));
            record.setPlayerName(player);
            record.setAmount(amount);
            record.setTargetKm(targetKm);
            record.setCashedOut(false);
            record.setLost(false);
            record.setPlacedAt(Instant.now());
            BetEntity saved = betRepository.save(record);
            betRecordIdByPlayer.put(playerKey, saved.getId());
        }

        addEvent(player + " locked " + targetKm + "km with " + amount + " credits");
        return new BetResult(true, "Bet accepted.", player, amount, targetKm);
    }

    public synchronized GameStateSnapshot getCurrentSnapshot() {
        return snapshot();
    }

    public synchronized void forceLaunch() {
        if (phase == GamePhase.BETTING) {
            launch();
            broadcastSnapshot();
        }
    }

    public synchronized void forceStartNewRound() {
        startNewRound();
        broadcastSnapshot();
    }

    public synchronized boolean setPaused(boolean paused) {
        this.paused = paused;
        return this.paused;
    }

    public synchronized boolean isPaused() {
        return paused;
    }

    public synchronized void triggerMoonMissionNextRound() {
        forceNextMoonMission = true;
        addEvent("Admin queued MOON MISSION for next round");
        broadcastSnapshot();
    }

    public synchronized void triggerGoldenRocketNextRound() {
        forceNextGoldenRocket = true;
        addEvent("Admin queued GOLDEN ROCKET for next round");
        broadcastSnapshot();
    }

    private void launch() {
        phase = GamePhase.FLYING;
        ticksInPhase = 0;
        soloModeRound = betsByPlayer.size() <= 1;
        speedKmPerSec = 6 + random.nextDouble() * 3;
        crashAltitudeKm = generateCrashAltitude();
        persistRoundState();
        if (soloModeRound) {
            addEvent("Launch started. SOLO mode auto-cashout armed.");
        } else {
            addEvent("Launch started. COMPETITIVE winner-takes-pool mode armed.");
        }
    }

    private void explode() {
        phase = GamePhase.EXPLODED;
        ticksInPhase = 0;

        long explodedAt = Math.round(altitudeKm);
        addEvent("EXPLODED at " + explodedAt + "km");

        if (activeRoundRecordId != null) {
            roundRepository.findById(activeRoundRecordId).ifPresent(round -> {
                round.setPhase(GamePhase.EXPLODED);
                round.setExplodedAtKm(explodedAt);
                round.setFinishedAt(Instant.now());
                roundRepository.save(round);
            });
        }

        if (soloModeRound) {
            resolveSoloRoundOnExplode(explodedAt);
            return;
        }

        resolveCompetitiveRoundOnExplode(explodedAt);
    }

    private void startNewRound() {
        roundId++;
        finalizePreviousRoundIfNeeded();

        phase = GamePhase.BETTING;
        ticksInPhase = 0;
        altitudeKm = 0;
        speedKmPerSec = 0;
        crashAltitudeKm = 0;
        betsByPlayer.clear();
        betRecordIdByPlayer.clear();
        soloModeRound = false;

        moonMission = forceNextMoonMission || random.nextDouble() < 0.06;
        goldenRocket = forceNextGoldenRocket || random.nextDouble() < 0.015;
        forceNextMoonMission = false;
        forceNextGoldenRocket = false;

        RoundEntity round = new RoundEntity();
        round.setRoundNumber(roundId);
        round.setPhase(GamePhase.BETTING);
        round.setMoonMission(moonMission);
        round.setGoldenRocket(goldenRocket);
        round.setStartedAt(Instant.now());
        RoundEntity saved = roundRepository.save(round);
        activeRoundRecordId = saved.getId();

        if (moonMission) {
            addEvent("MOON MISSION round activated");
        }
        if (goldenRocket) {
            addEvent("Golden Rocket detected");
        }
        addEvent("NEW LAUNCH - betting open for 30 seconds");
    }

    private void finalizePreviousRoundIfNeeded() {
        if (activeRoundRecordId == null) {
            return;
        }
        roundRepository.findById(activeRoundRecordId).ifPresent(round -> {
            if (round.getFinishedAt() == null) {
                round.setFinishedAt(Instant.now());
                roundRepository.save(round);
            }
        });
    }

    private long generateCrashAltitude() {
        double roll = random.nextDouble();
        double base = Math.pow(1.0 - roll, -1.25);
        long result = (long) (base * 120);

        if (moonMission) {
            result = (long) (result * 1.8);
        }
        if (goldenRocket) {
            result = (long) (result * 2.3);
        }

        return Math.max(80, Math.min(result, 250_000));
    }

    private void processCashouts() {
        if (!soloModeRound) {
            return;
        }

        long currentAltitude = Math.round(altitudeKm);
        for (Map.Entry<String, Bet> entry : betsByPlayer.entrySet()) {
            Bet bet = entry.getValue();
            if (bet.isCashedOut()) {
                continue;
            }
            if (currentAltitude >= bet.getTargetKm()) {
                double multiplier = payoutMultiplier(bet.getTargetKm());
                long payout = Math.round(bet.getAmount() * multiplier);
                bet.markCashedOut(payout);
                addEvent("SUCCESS: " + bet.getPlayer() + " reached " + bet.getTargetKm() + "km (" + payout + ")");
                markBetRecordAsWinner(entry.getKey(), payout);
            }
        }
    }

    private void resolveSoloRoundOnExplode(long explodedAt) {
        for (Map.Entry<String, Bet> entry : betsByPlayer.entrySet()) {
            Bet bet = entry.getValue();
            if (!bet.isCashedOut() && bet.getTargetKm() > explodedAt) {
                bet.markLost();
                addEvent(bet.getPlayer() + " lost at " + bet.getTargetKm() + "km");
                markBetRecordAsLost(entry.getKey());
            }
        }
    }

    private void resolveCompetitiveRoundOnExplode(long explodedAt) {
        List<Map.Entry<String, Bet>> entries = new ArrayList<>(betsByPlayer.entrySet());
        long poolAmount = entries.stream().mapToLong(e -> e.getValue().getAmount()).sum();

        List<Map.Entry<String, Bet>> eligible = entries.stream()
                .filter(e -> e.getValue().getTargetKm() <= explodedAt)
                .toList();

        if (eligible.isEmpty()) {
            for (Map.Entry<String, Bet> entry : entries) {
                entry.getValue().markLost();
                markBetRecordAsLost(entry.getKey());
            }
            addEvent("No eligible winner this round. Pool was " + poolAmount);
            return;
        }

        long highestSafeTarget = eligible.stream()
                .mapToLong(e -> e.getValue().getTargetKm())
                .max()
                .orElse(0L);

        List<Map.Entry<String, Bet>> contenders = eligible.stream()
                .filter(e -> e.getValue().getTargetKm() == highestSafeTarget)
                .sorted((a, b) -> {
                    int byAmount = Long.compare(b.getValue().getAmount(), a.getValue().getAmount());
                    if (byAmount != 0) {
                        return byAmount;
                    }
                    return a.getValue().getPlayer().compareToIgnoreCase(b.getValue().getPlayer());
                })
                .toList();

        long contenderAmountTotal = contenders.stream().mapToLong(e -> e.getValue().getAmount()).sum();
        Map<String, Long> payouts = new HashMap<>();
        long assigned = 0;
        for (Map.Entry<String, Bet> contender : contenders) {
            long share = (poolAmount * contender.getValue().getAmount()) / contenderAmountTotal;
            payouts.put(contender.getKey(), share);
            assigned += share;
        }

        long remainder = poolAmount - assigned;
        for (int i = 0; i < remainder; i++) {
            String key = contenders.get(i % contenders.size()).getKey();
            payouts.put(key, payouts.get(key) + 1);
        }

        for (Map.Entry<String, Bet> entry : entries) {
            String playerKey = entry.getKey();
            Bet bet = entry.getValue();

            if (payouts.containsKey(playerKey)) {
                long payout = payouts.get(playerKey);
                bet.markCashedOut(payout);
                markBetRecordAsWinner(playerKey, payout);
                addEvent("WIN: " + bet.getPlayer() + " at " + highestSafeTarget + "km won " + payout);
            } else {
                bet.markLost();
                markBetRecordAsLost(playerKey);
            }
        }

        if (contenders.size() > 1) {
            addEvent("Tie at " + highestSafeTarget + "km split by bet amount");
        }
    }

    private void markBetRecordAsWinner(String playerKey, long payout) {
        Long recordId = betRecordIdByPlayer.get(playerKey);
        if (recordId == null) {
            return;
        }
        betRepository.findById(recordId).ifPresent(record -> {
            record.setCashedOut(true);
            record.setLost(false);
            record.setPayout(payout);
            record.setCashedOutAt(Instant.now());
            betRepository.save(record);
        });
    }

    private void markBetRecordAsLost(String playerKey) {
        Long recordId = betRecordIdByPlayer.get(playerKey);
        if (recordId == null) {
            return;
        }
        betRepository.findById(recordId).ifPresent(record -> {
            record.setLost(true);
            betRepository.save(record);
        });
    }

    private void persistRoundState() {
        if (activeRoundRecordId == null) {
            return;
        }
        roundRepository.findById(activeRoundRecordId).ifPresent(round -> {
            round.setPhase(phase);
            round.setCrashAltitudeKm(crashAltitudeKm);
            roundRepository.save(round);
        });
    }

    private double payoutMultiplier(long targetKm) {
        double multiplier = 1.08 + Math.log10(targetKm + 10) * 1.45;
        if (moonMission) {
            multiplier *= 1.25;
        }
        if (goldenRocket) {
            multiplier *= 1.35;
        }
        return Math.max(multiplier, 1.1);
    }

    private void addEvent(String event) {
        recentEvents.addFirst(event);
        while (recentEvents.size() > MAX_EVENTS) {
            recentEvents.removeLast();
        }
    }

    private void broadcastSnapshot() {
        messagingTemplate.convertAndSend("/topic/state", snapshot());
    }

    private GameStateSnapshot snapshot() {
        List<Bet> sorted = new ArrayList<>(betsByPlayer.values());
        sorted.sort(Comparator.comparingLong(Bet::getTargetKm).reversed());

        List<TopTargetView> topTargets = sorted.stream()
                .limit(8)
                .map(b -> new TopTargetView(b.getPlayer(), b.getTargetKm(), b.getAmount()))
                .toList();

        List<TopTargetView> markers = sorted.stream()
            .filter(b -> !b.isCashedOut() && !b.isLost())
                .map(b -> new TopTargetView(b.getPlayer(), b.getTargetKm(), b.getAmount()))
                .toList();

        Integer bettingLeft = phase == GamePhase.BETTING
                ? Math.max(0, (BETTING_TICKS - ticksInPhase) / 10)
                : null;

        Long crash = phase == GamePhase.EXPLODED ? crashAltitudeKm : null;

        return new GameStateSnapshot(
                roundId,
                phase,
                Math.round(altitudeKm),
                Math.round(speedKmPerSec * 100.0) / 100.0,
                bettingLeft,
                crash,
                moonMission,
                goldenRocket,
                flameFrame,
                topTargets,
                markers,
                List.copyOf(recentEvents)
        );
    }
}
