package com.racetothemoon.model;

import java.util.List;

public record GameStateSnapshot(
        long roundId,
        GamePhase phase,
        long altitudeKm,
        double speedKmPerSec,
        Integer bettingSecondsRemaining,
        Long crashAltitudeKm,
        boolean moonMission,
        boolean goldenRocket,
        int flameFrame,
        List<TopTargetView> topTargets,
        List<TopTargetView> activeMarkers,
        List<String> recentEvents
) {
}
