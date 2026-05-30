package com.racetothemoon.model;

public record BetResult(boolean accepted, String message, String player, long amount, long targetKm) {
}
