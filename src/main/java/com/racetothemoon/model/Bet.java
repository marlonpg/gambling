package com.racetothemoon.model;

public class Bet {
    private final String player;
    private final long amount;
    private final long targetKm;
    private boolean cashedOut;
    private long payout;

    public Bet(String player, long amount, long targetKm) {
        this.player = player;
        this.amount = amount;
        this.targetKm = targetKm;
    }

    public String getPlayer() {
        return player;
    }

    public long getAmount() {
        return amount;
    }

    public long getTargetKm() {
        return targetKm;
    }

    public boolean isCashedOut() {
        return cashedOut;
    }

    public void markCashedOut(long payout) {
        this.cashedOut = true;
        this.payout = payout;
    }

    public long getPayout() {
        return payout;
    }
}
