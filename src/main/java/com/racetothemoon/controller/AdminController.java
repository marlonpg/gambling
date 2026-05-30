package com.racetothemoon.controller;

import com.racetothemoon.model.AdminCommandResult;
import com.racetothemoon.service.GameEngineService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final GameEngineService gameEngineService;

    public AdminController(GameEngineService gameEngineService) {
        this.gameEngineService = gameEngineService;
    }

    @PostMapping("/force-launch")
    public AdminCommandResult forceLaunch() {
        gameEngineService.forceLaunch();
        return new AdminCommandResult(true, "Launch command applied.");
    }

    @PostMapping("/new-round")
    public AdminCommandResult newRound() {
        gameEngineService.forceStartNewRound();
        return new AdminCommandResult(true, "Started new round.");
    }

    @PostMapping("/pause")
    public AdminCommandResult pause() {
        gameEngineService.setPaused(true);
        return new AdminCommandResult(true, "Game tick paused.");
    }

    @PostMapping("/resume")
    public AdminCommandResult resume() {
        gameEngineService.setPaused(false);
        return new AdminCommandResult(true, "Game tick resumed.");
    }

    @PostMapping("/next-moon")
    public AdminCommandResult nextMoon() {
        gameEngineService.triggerMoonMissionNextRound();
        return new AdminCommandResult(true, "Moon mission queued for next round.");
    }

    @PostMapping("/next-golden")
    public AdminCommandResult nextGolden() {
        gameEngineService.triggerGoldenRocketNextRound();
        return new AdminCommandResult(true, "Golden rocket queued for next round.");
    }
}
