package com.tema1.Players;

import com.tema1.goods.GoodsFactory;

import java.util.ArrayList;
import java.util.List;

import static com.tema1.common.Constants.MINIMAL_MONEY;
import static com.tema1.common.Constants.MAX_LEGALGOOD;
import static com.tema1.common.Constants.MAX_BAG;
import static com.tema1.common.Constants.APPLE;

public class Greedy extends Player {
    Greedy(final int id, final String type) {
        super(id, type);
    }

    // implementarea strategiei de comerciant a lui greedy identica cu basic cu o exceptie la final
    public void isVendor(final int round) {
        int[] freqLegal = new int[MAX_LEGALGOOD];
        int maxFreq = 0;
        int maxProfit = 0;
        int maxId = 0;
        int goodProfit;

        ArrayList<Integer> inHand = this.getInHand();

        for (int currGoodId : inHand) {
            goodProfit = GoodsFactory.getInstance().getGoodsById(currGoodId).getProfit();

            if (currGoodId < MAX_LEGALGOOD) {
                freqLegal[currGoodId]++;
                if (maxFreq == freqLegal[currGoodId]) {
                    if (maxProfit == goodProfit) {
                        if (maxId < currGoodId) {
                            maxId = currGoodId;
                        }
                    }
                    if (maxProfit < goodProfit) {
                        maxProfit = goodProfit;
                        maxId = currGoodId;
                    }
                }
                if (maxFreq < freqLegal[currGoodId]) {
                    maxFreq = freqLegal[currGoodId];
                    maxId = currGoodId;
                    maxProfit = goodProfit;
                }
            }
        }

        if (maxFreq != 0) {
            for (int i = 0; i < maxFreq && i < MAX_BAG; ++i) {
                this.addGoodInBag(maxId);
            }
            this.updateDeclaredGood(maxId);
        } else {
            maxProfit = 0;
            maxId = 0;

            for (int currGoodId : inHand) {
                if (maxProfit < GoodsFactory.getInstance().getGoodsById(currGoodId).getProfit()) {
                    maxProfit = GoodsFactory.getInstance().getGoodsById(currGoodId).getProfit();
                    maxId = currGoodId;
                }
            }

            if (this.getTotalMoney()
                    >= GoodsFactory.getInstance().getGoodsById(maxId).getPenalty()) {
                this.addGoodInBag(maxId);
            }

            this.updateDeclaredGood(APPLE);
            this.removeInHand(maxId);
        }

        // daca runda de joc e para si sacul nu e plin deja si greedy isi permite o penalizare
        if (round % 2 == 0 && maxFreq < MAX_BAG && this.getTotalMoney()
                >= GoodsFactory.getInstance().getGoodsById(maxId).getPenalty()) {
            maxProfit = 0;
            maxId = 0;

            // cauta bunul ilegal de profit maxim
            for (int currGoodId : inHand) {
                goodProfit = GoodsFactory.getInstance().getGoodsById(currGoodId).getProfit();
                if (currGoodId > MAX_LEGALGOOD) {
                    if (maxProfit < goodProfit) {
                        maxProfit = goodProfit;
                        maxId = currGoodId;
                    }
                }
            }
            // daca am gasit macar bun ilegal il adaug
            if (maxId != 0) {
                this.addGoodInBag(maxId);
            }
        }
        this.discardHand();
    }

    // implementarea strategiei de serif a lui greedy identica cu basic cu o verificare diferita
    public void isSheriff(final ArrayList<Player> players, final List<Integer> allGoodsId) {
        for (Player currPlayer : players) {
            if (currPlayer.getId() != this.getId()) {
                    checkPlayer(currPlayer, allGoodsId);
                }
        }
    }

    // implementarea modului in care greedy verifica ceilalti jucatori
    public void checkPlayer(final Player player, final List<Integer> allGoodsId) {
        // inatai verifica daca jucatorul ii ofera mita
        if (player.getBribe() == 0) {
            // daca nu ii ofera il verifica exact ca basic
            if (this.getTotalMoney() >= MINIMAL_MONEY) {
                boolean fair = true;
                int penaltyPlayer = 0;
                int penaltySheriff = 0;
                for (int currGoodId : player.getInBag()) {
                    if (currGoodId != player.getDeclaredGood() || currGoodId > MAX_LEGALGOOD) {
                        fair = false;
                        penaltyPlayer +=
                                GoodsFactory.getInstance().getGoodsById(currGoodId).getPenalty();
                        allGoodsId.add(currGoodId);
                    } else {
                        player.addGoodInTable(currGoodId);
                    }
                }
                int decGood = player.getDeclaredGood();
                penaltySheriff +=
                        (GoodsFactory.getInstance().getGoodsById(decGood).getPenalty()
                                * player.getInBag().size());
                if (fair) {
                    this.updateMoney(-penaltySheriff);
                    player.updateMoney(penaltySheriff);
                } else {
                    this.updateMoney(penaltyPlayer);
                    player.updateMoney(-penaltyPlayer);
                }
            } else {
                for (int currGoodId : player.getInBag()) {
                    player.addGoodInTable(currGoodId);
                }
            }
        } else {
            this.updateMoney(player.getBribe());
            // cei ce ii ofera mita pun toate bunurile din sac pe taraba
            for (int currGoodId : player.getInBag()) {
                player.addGoodInTable(currGoodId);
            }
        }
        player.discardBag();
    }
}
