package com.racetothemoon.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class BetRequest {
    @NotBlank(message = "Player name is required")
    private String player;

    private String command;

    @Min(value = 1, message = "Amount must be at least 1")
    private Long amount;

    @Min(value = 20, message = "Target must be at least 20km")
    private Long targetKm;

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getTargetKm() {
        return targetKm;
    }

    public void setTargetKm(Long targetKm) {
        this.targetKm = targetKm;
    }
}
