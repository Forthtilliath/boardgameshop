package com.bgs.boardgameshop.admin;

import com.bgs.boardgameshop.admin.dto.AdminGameRequest;
import com.bgs.boardgameshop.game.GameResponse;
import com.bgs.boardgameshop.game.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/games")
public class AdminGameController {

    private final AdminGameService adminGameService;
    private final GameService gameService;

    public AdminGameController(AdminGameService adminGameService, GameService gameService) {
        this.adminGameService = adminGameService;
        this.gameService = gameService;
    }

    @GetMapping
    public List<GameResponse> getGames() {
        return gameService.getGames(null);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameResponse createGame(@Valid @RequestBody AdminGameRequest request) {
        return adminGameService.createGame(request);
    }

    @PutMapping("/{id}")
    public GameResponse updateGame(@PathVariable Long id, @Valid @RequestBody AdminGameRequest request) {
        return adminGameService.updateGame(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@PathVariable Long id) {
        adminGameService.deleteGame(id);
    }
}
