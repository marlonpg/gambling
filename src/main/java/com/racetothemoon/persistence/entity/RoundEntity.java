package com.racetothemoon.persistence.entity;

import com.racetothemoon.model.GamePhase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "rounds")
public class RoundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roundNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GamePhase phase;

    @Column(nullable = false)
    private boolean moonMission;

    @Column(nullable = false)
    private boolean goldenRocket;

    private Long crashAltitudeKm;

    private Long explodedAtKm;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant finishedAt;

    public Long getId() {
        return id;
    }

    public Long getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Long roundNumber) {
        this.roundNumber = roundNumber;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public boolean isMoonMission() {
        return moonMission;
    }

    public void setMoonMission(boolean moonMission) {
        this.moonMission = moonMission;
    }

    public boolean isGoldenRocket() {
        return goldenRocket;
    }

    public void setGoldenRocket(boolean goldenRocket) {
        this.goldenRocket = goldenRocket;
    }

    public Long getCrashAltitudeKm() {
        return crashAltitudeKm;
    }

    public void setCrashAltitudeKm(Long crashAltitudeKm) {
        this.crashAltitudeKm = crashAltitudeKm;
    }

    public Long getExplodedAtKm() {
        return explodedAtKm;
    }

    public void setExplodedAtKm(Long explodedAtKm) {
        this.explodedAtKm = explodedAtKm;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }
}
