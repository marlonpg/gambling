package com.racetothemoon.model;

import java.time.Instant;

public record BetHistoryView(
        Long id,
        String playerName,
        long amount,
        long targetKm,
        boolean cashedOut,
        Long payout,
        boolean lost,
        Instant placedAt,
        Instant cashedOutAt
) {
}
