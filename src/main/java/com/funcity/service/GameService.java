package com.funcity.service;

import com.funcity.dao.GameDAO;
import com.funcity.model.Game;
import com.funcity.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private final GameDAO gameDAO;

    public GameService() {
        this.gameDAO = new GameDAO();
    }

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public Game addGame(String name, String category, String description, BigDecimal price, boolean dispensesTickets) {
        ValidationUtil.requireNonEmpty(name, "Game Name");
        ValidationUtil.requireNonEmpty(category, "Category");
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Game price cannot be negative.");
        }

        Game game = new Game(0, name.trim(), category.trim(), description, price, dispensesTickets, "ACTIVE");
        Game created = gameDAO.create(game);
        logger.info("Added new game: {} ($ {})", created.getName(), created.getPricePerPlay());
        return created;
    }

    public boolean updateGame(Game game) {
        ValidationUtil.requireNonEmpty(game.getName(), "Game Name");
        ValidationUtil.requireNonEmpty(game.getCategory(), "Category");
        if (game.getPricePerPlay() == null || game.getPricePerPlay().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Game price cannot be negative.");
        }
        return gameDAO.update(game);
    }

    public boolean deleteGame(int gameId) {
        return gameDAO.delete(gameId);
    }

    public Optional<Game> getGameById(int gameId) {
        return gameDAO.findById(gameId);
    }

    public List<Game> getAllGames() {
        return gameDAO.findAll();
    }

    public List<Game> getActiveGames() {
        return gameDAO.findActive();
    }
}

