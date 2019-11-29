package com.tema1.common;

import com.tema1.Players.Player;

import java.util.Comparator;

public final class PlayerComparator implements Comparator<Player> {
    @Override
    public int compare(final Player p1, final Player p2) {
        if (p1.getTotalMoney() == p2.getTotalMoney()) {
            return p1.getId() - p2.getId();
        }
        return p2.getTotalMoney() - p1.getTotalMoney();
    }
}
