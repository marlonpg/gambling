package com.racetothemoon.controller;

import com.racetothemoon.model.BetRequest;
import com.racetothemoon.model.BetResult;
import com.racetothemoon.model.GameStateSnapshot;
import com.racetothemoon.service.GameEngineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class GameController {

    private static final Pattern BET_COMMAND_PATTERN =
            Pattern.compile("^\\s*!bet\\s+(\\d+)\\s+at\\s+(\\d+)\\s*km\\s*$", Pattern.CASE_INSENSITIVE);

    private final GameEngineService gameEngineService;

    public GameController(GameEngineService gameEngineService) {
        this.gameEngineService = gameEngineService;
    }

    @PostMapping("/bets")
    public ResponseEntity<BetResult> placeBet(@Valid @RequestBody BetRequest request) {
        long amount;
        long targetKm;

        if (request.getCommand() != null && !request.getCommand().isBlank()) {
            ParsedBet parsed = parseCommand(request.getCommand());
            if (parsed == null) {
                return ResponseEntity.badRequest().body(new BetResult(
                        false,
                        "Invalid command. Example: !bet 100 at 500km",
                        request.getPlayer(),
                        0,
                        0
                ));
            }
            amount = parsed.amount();
            targetKm = parsed.targetKm();
        } else if (request.getAmount() != null && request.getTargetKm() != null) {
            amount = request.getAmount();
            targetKm = request.getTargetKm();
        } else {
            return ResponseEntity.badRequest().body(new BetResult(
                    false,
                    "Provide either command or amount/targetKm fields.",
                    request.getPlayer(),
                    0,
                    0
            ));
        }

        BetResult result = gameEngineService.placeBet(request.getPlayer().trim(), amount, targetKm);
        if (!result.accepted()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/state")
    public GameStateSnapshot getState() {
        return gameEngineService.getCurrentSnapshot();
    }

    private ParsedBet parseCommand(String command) {
        Matcher matcher = BET_COMMAND_PATTERN.matcher(command);
        if (!matcher.matches()) {
            return null;
        }
        long amount = Long.parseLong(matcher.group(1));
        long targetKm = Long.parseLong(matcher.group(2));
        return new ParsedBet(amount, targetKm);
    }

    private record ParsedBet(long amount, long targetKm) {
    }
}
