package com.tema1.Players;

import java.util.ArrayList;
import java.util.List;

import static com.tema1.common.Constants.MAX_HAND;
import static com.tema1.common.Constants.START_MONEY;

public abstract class Player {

    // am definit atributele oricarui player
    private int id;
    private int totalMoney;
    private int declaredGood;
    private String type;
    private int bribe;
    // si unde o sa aiba cartile de-a lungul jocului
    private ArrayList<Integer> inHand;
    private ArrayList<Integer> inBag;
    private ArrayList<Integer> inTable;

    Player(final int id, final String type) {
        this.type = type;
        this.id = id;
        bribe = 0;
        totalMoney = START_MONEY;
        declaredGood = -1;
        inHand = new ArrayList<>();
        inBag = new ArrayList<>();
        inTable = new ArrayList<>();
    }

    // seteaza o noua mita dar nu inainte de a aduna la loc la suma de bani vechia mita
    final void setBribe(final int addBribe) {
        totalMoney += bribe;
        bribe = addBribe;
        totalMoney -= addBribe;
    }

    public void clearBribe() {
        bribe = 0;
    }

    final int getBribe() {
        return bribe;
    }

    public String getType() {
        return type;
    }

    final ArrayList<Integer> getInHand() {
        return inHand;
    }

    // sterge din mana prima aparitie a unui bun
    final void removeInHand(final int goodId) {
        for (int i = 0;  i < inHand.size(); ++i) {
            if (inHand.get(i) == goodId) {
                inHand.remove(i);
                break;
            }
        }
    }

    final void discardHand() {
        while (!inHand.isEmpty()) {
            inHand.remove(0);
        }
    }

    final void discardBag() {
        while (!inBag.isEmpty()) {
            inBag.remove(0);
        }
    }

    final ArrayList<Integer> getInBag() {
        return inBag;
    }

    final void addGoodInBag(final int goodId) {
        inBag.add(goodId);
    }

    public ArrayList<Integer> getInTable() {
        return inTable;
    }

    public void addGoodInTable(final int goodId) {
        inTable.add(goodId);
    }

    public int getId() {
        return id;
    }

    final int getDeclaredGood() {
        return declaredGood;
    }

    public int getTotalMoney() {
        return totalMoney;
    }

    public void updateMoney(final int addMoney) {
        totalMoney += addMoney;
    }

    final void updateDeclaredGood(final int decGood) {
        declaredGood = decGood;
    }

    // adauga bunuri noi in mana pana la 10
    public final void drawGoods(final List<Integer> allGoods) {
        for (int i = 0; i < MAX_HAND; ++i) {
            inHand.add(allGoods.get(0));
            allGoods.remove(0);
        }
    }

    public abstract void checkPlayer(Player player, List<Integer> allGoodsId);

    public abstract void isVendor(int round);

    public abstract void isSheriff(ArrayList<Player> players, List<Integer> allGoodsId);

    public boolean equals(final Player other) {
        if (this.getId() == other.getId()) {
            return true;
        }
        return false;
    }
}
