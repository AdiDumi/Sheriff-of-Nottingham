package com.tema1.Players;

import com.tema1.goods.Goods;
import com.tema1.goods.GoodsFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.tema1.common.Constants.MAX_BRIBE;
import static com.tema1.common.Constants.MIN_BRIBE;
import static com.tema1.common.Constants.MINIMAL_MONEY;
import static com.tema1.common.Constants.MAX_LEGALGOOD;
import static com.tema1.common.Constants.MAX_BAG;
import static com.tema1.common.Constants.APPLE;
import static com.tema1.common.Constants.MINIM_BRIBE;

public class Bribed extends Player {
    Bribed(final int id, final String type) {
        super(id, type);
    }

    // implementarea strategiei de comerciant a lui bribe
    public void isVendor(final int round) {
        List<Integer> legalGoods = new ArrayList<>();
        List<Integer> illegalGoods = new ArrayList<>();
        ArrayList<Integer> inHand = this.getInHand();
        int maxFreq = 0;
        int maxProfit = 0;
        int maxId = 0;
        int goodProfit;
        // retin bunurile legale si ilegale din mana separat
        for (int currGoodId : inHand) {
            if (currGoodId > MAX_LEGALGOOD) {
                illegalGoods.add(currGoodId);
            } else {
                legalGoods.add(currGoodId);
            }
        }
        // daca bribe are doar bunuri legale sau nu are macar bani sa dea mita fara sa ajunga pe 0
        if (illegalGoods.isEmpty() || this.getTotalMoney() <= MINIM_BRIBE) {
            // aplica strategia jucatorului basic
            int[] freqLegal = new int[MAX_LEGALGOOD];
            for (int currGoodId : legalGoods) {
                goodProfit = GoodsFactory.getInstance().getGoodsById(currGoodId).getProfit();
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
            if (maxFreq != 0) {
                for (int i = 0; i < maxFreq && i < MAX_BAG; ++i) {
                    this.addGoodInBag(maxId);
                }
                this.updateDeclaredGood(maxId);
            } else {
                maxProfit = 0;
                maxId = 0;
                for (int currIllegalGood : illegalGoods) {
                    goodProfit =
                            GoodsFactory.getInstance().getGoodsById(currIllegalGood).getProfit();
                    if (goodProfit > maxProfit) {
                        maxProfit = goodProfit;
                        maxId = currIllegalGood;
                    }
                }
                this.addGoodInBag(maxId);
                this.updateDeclaredGood(APPLE);
            }
        } else {
            // daca are bani de mita sortez ilegalele in functie de profit
            for (int i = 0; i < illegalGoods.size() - 1; ++i) {
                for (int j = 0; j < illegalGoods.size() - i - 1; ++j) {
                    Goods good1 = GoodsFactory.getInstance().getGoodsById(illegalGoods.get(j));
                    Goods good2 = GoodsFactory.getInstance().getGoodsById(illegalGoods.get(j + 1));
                    if (good1.getProfit() < good2.getProfit()) {
                        Collections.swap(illegalGoods, j, j + 1);
                    }
                }
            }
            // si legalele dupa profit si dupa id
            for (int i = 0; i < legalGoods.size() - 1; ++i) {
                for (int j = 0; j < legalGoods.size() - i - 1; ++j) {
                    Goods good1 = GoodsFactory.getInstance().getGoodsById(legalGoods.get(j));
                    Goods good2 = GoodsFactory.getInstance().getGoodsById(legalGoods.get(j + 1));
                    if (good1.getProfit() < good2.getProfit()) {
                        Collections.swap(legalGoods, j, j + 1);
                    } else {
                        if (good1.getProfit() == good2.getProfit() && good1.getId()
                                < good2.getId()) {
                            Collections.swap(legalGoods, j, j + 1);
                        }
                    }
                }
            }
            // setez mita minima
            int good;
            int nrIllegal = 1;
            this.setBribe(MIN_BRIBE);
            // pe masura ce adaug ilegale sac
            do {
                good = illegalGoods.get(0);
                this.addGoodInBag(good);
                illegalGoods.remove(0);
                // verific sa nu trebuiasca sa schimb mita
                if (nrIllegal > 2 && getTotalMoney() > MINIM_BRIBE) {
                    this.setBribe(MAX_BRIBE);
                }
                nrIllegal++;
                // si verific daca mai imi permit sa adaug sau mai am ilegale
            }
            while (this.getTotalMoney() + this.getBribe()
                    > nrIllegal * GoodsFactory.getInstance().getGoodsById(good).getPenalty()
                    && this.getInBag().size() < MAX_BAG && !illegalGoods.isEmpty());

            int penalty = (nrIllegal - 1)
                    * GoodsFactory.getInstance().getGoodsById(good).getPenalty();

            // daca am legale
            if (!legalGoods.isEmpty()) {
                int nrLegal = 1;
                // verific daca imi permit penalizarea acumulata sau daca mia e loc in sac
                while (this.getTotalMoney() + this.getBribe() > penalty
                        + GoodsFactory.getInstance().getGoodsById(legalGoods.get(0)).getPenalty()
                        * nrLegal && this.getInBag().size() < MAX_BAG) {
                    this.addGoodInBag(legalGoods.get(0));
                    legalGoods.remove(0);
                    nrLegal++;
                }
            }
            // declara ca a pus in sac mere
            this.updateDeclaredGood(APPLE);
        }
        this.discardHand();
    }

    // implementarea strategiei de serif a lui bribe
    public void isSheriff(final ArrayList<Player> players, final List<Integer> allGoodsId) {
        // daca bribe e primul jucator o sa-l verifice pe urmatorul si ultimul jucator din vector
        if (this.getId() == 0) {
            checkPlayer(players.get(players.size() - 1), allGoodsId);
            if (players.size() > 2) {
                checkPlayer(players.get(1), allGoodsId);
            }
            // ceilalti jucatori isi pun bunurile pe taraba si isi iau mita inapoi
            for (int i = 2; i < players.size() - 1; ++i) {
                this.updateMoney(players.get(i).getBribe());
                for (int currGoodId : players.get(i).getInBag()) {
                    players.get(i).addGoodInTable(currGoodId);
                }
            }
        } else {
            // daca bribe e ultimul jucator verifica jucatorul precedent si pe primul
            if (this.getId() == players.size() - 1) {
                checkPlayer(players.get(players.size() - 2), allGoodsId);
                if (players.size() > 2) {
                    checkPlayer(players.get(0), allGoodsId);
                }
                for (int i = 1; i < players.size() - 2; ++i) {
                    this.updateMoney(players.get(i).getBribe());
                    for (int currGoodId : players.get(i).getInBag()) {
                        players.get(i).addGoodInTable(currGoodId);
                    }
                }
            } else {
                // daca nu e nici primul nici ultimul verifica doar jucatorii adiacenti cu el
                for (Player currPlayer : players) {
                    if (currPlayer.getId() != this.getId()) {
                        if (this.getId() - 1 == currPlayer.getId()
                                || this.getId() + 1 == currPlayer.getId()) {
                            checkPlayer(currPlayer, allGoodsId);
                        } else {
                            for (int currGoodId : currPlayer.getInBag()) {
                                currPlayer.addGoodInTable(currGoodId);
                            }
                            this.updateMoney(currPlayer.getBribe());
                        }

                    }
                }
            }
        }
        // toti jucatorii, verificati sau nu, ard cartile ramase in sac
        for (Player currPlayer : players) {
            currPlayer.discardBag();
        }
    }

    // implemenatrea modului in care bribe verifica jucatorii, identica cu a lui basic
    public void checkPlayer(final Player player, final List<Integer> allGoodsId) {
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
            penaltySheriff +=
                    (GoodsFactory.getInstance().getGoodsById(player.getDeclaredGood()).getPenalty()
                            * player.getInBag().size());
            if (fair) {
                this.updateMoney(-penaltySheriff);
                player.updateMoney(penaltySheriff);
            } else {
                this.updateMoney(penaltyPlayer);
                player.updateMoney(-penaltyPlayer);
                player.updateMoney(player.getBribe());
            }
        } else {
            player.updateMoney(player.getBribe());
            for (int currGoodId : player.getInBag()) {
                player.addGoodInTable(currGoodId);
            }
        }
    }
}
