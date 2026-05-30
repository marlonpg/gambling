package com.racetothemoon.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "bets")
public class BetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    private RoundEntity round;

    @Column(nullable = false, length = 64)
    private String playerName;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private long targetKm;

    @Column(nullable = false)
    private boolean cashedOut;

    private Long payout;

    @Column(nullable = false)
    private boolean lost;

    @Column(nullable = false)
    private Instant placedAt;

    private Instant cashedOutAt;

    public Long getId() {
        return id;
    }

    public RoundEntity getRound() {
        return round;
    }

    public void setRound(RoundEntity round) {
        this.round = round;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getTargetKm() {
        return targetKm;
    }

    public void setTargetKm(long targetKm) {
        this.targetKm = targetKm;
    }

    public boolean isCashedOut() {
        return cashedOut;
    }

    public void setCashedOut(boolean cashedOut) {
        this.cashedOut = cashedOut;
    }

    public Long getPayout() {
        return payout;
    }

    public void setPayout(Long payout) {
        this.payout = payout;
    }

    public boolean isLost() {
        return lost;
    }

    public void setLost(boolean lost) {
        this.lost = lost;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(Instant placedAt) {
        this.placedAt = placedAt;
    }

    public Instant getCashedOutAt() {
        return cashedOutAt;
    }

    public void setCashedOutAt(Instant cashedOutAt) {
        this.cashedOutAt = cashedOutAt;
    }
}
