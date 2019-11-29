package com.tema1.Players;

public abstract class PlayerFactory {
    public static Player getPlayer(final String playerName, final int id) {
        switch (playerName) {
            case "basic":
                return new Basic(id, playerName);

            case "greedy":
                return new Greedy(id, playerName);

            case "bribed":
                return new Bribed(id, playerName);

            default:
                return null;
        }
    }
}
