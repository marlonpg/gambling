package com.racetothemoon.model;

import java.time.Instant;

public record RoundHistoryView(
        Long id,
        Long roundNumber,
        String phase,
        boolean moonMission,
        boolean goldenRocket,
        Long crashAltitudeKm,
        Long explodedAtKm,
        Instant startedAt,
        Instant finishedAt
) {
}
