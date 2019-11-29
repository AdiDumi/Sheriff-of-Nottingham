package com.tema1.Players;

import com.tema1.goods.GoodsFactory;

import java.util.ArrayList;
import java.util.List;

import static com.tema1.common.Constants.APPLE;
import static com.tema1.common.Constants.MAX_LEGALGOOD;
import static com.tema1.common.Constants.MINIMAL_MONEY;
import static com.tema1.common.Constants.MAX_BAG;

public class Basic extends Player {
    Basic(final int id, final String type) {
        super(id, type);
    }

    // implementarea strategiei de comerciant a jucatorului basic
    public void isVendor(final int round) {
        int[] freqLegal = new int[MAX_LEGALGOOD];
        int maxFreq = 0;
        int maxProfit = 0;
        int maxId = 0;
        int goodProfit;

        ArrayList<Integer> inHand = this.getInHand();

        for (int currGoodId : inHand) {
            goodProfit = GoodsFactory.getInstance().getGoodsById(currGoodId).getProfit();
            // parcurg mana jucatorului
            if (currGoodId < MAX_LEGALGOOD) {
                freqLegal[currGoodId]++;
                // iar cartilor legale pe care le gasesc le maresc fecventa
                // totodata calculez frecventa maxima si bunul ce o detine
                if (maxFreq == freqLegal[currGoodId]) {
                    if (maxProfit == goodProfit) {
                        // la frecvente si profit egale retin bunul cu id mic
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

        // daca am gasit macar un bun legal frecventa maxima o sa fie nenula
        if (maxFreq != 0) {
            // adaug acel bun in sac de cate ori l am gasit sau pana e plin sacul
            for (int i = 0; i < maxFreq && i < MAX_BAG; ++i) {
                this.addGoodInBag(maxId);
            }
            this.updateDeclaredGood(maxId);
        } else {
            maxProfit = 0;
            maxId = 0;
            // caut bunul ilegal de profit maxim
            for (int currGoodId : inHand) {
                if (maxProfit < GoodsFactory.getInstance().getGoodsById(currGoodId).getProfit()) {
                    maxProfit = GoodsFactory.getInstance().getGoodsById(currGoodId).getProfit();
                    maxId = currGoodId;
                }
            }
            // verific daca imi permit penalizarea pe el
            if (this.getTotalMoney()
                    >= GoodsFactory.getInstance().getGoodsById(maxId).getPenalty()) {
                this.addGoodInBag(maxId);
            }
            this.updateDeclaredGood(APPLE);
        }

        // ard cartile ramase in mana
        this.discardHand();
    }

    // implementarea strategiei de serif a jucatorului basic
    public void isSheriff(final ArrayList<Player> players, final List<Integer> allGoodsId) {
        // verifica toti ceilalti jucatori
        for (Player currPlayer : players) {
            if (currPlayer.getId() != this.getId()) {
                checkPlayer(currPlayer, allGoodsId);
            }
        }
    }

    // implementarea modului in care basic ii verifica pe ceilalti
    public void checkPlayer(final Player player, final List<Integer> allGoodsId) {
        /* daca basic isi permite sa ia penalty, daca comerciantul a spus adevarul pe nr maxim de
        bunuri legale in sac, il verifica
        */
        if (this.getTotalMoney() >= MINIMAL_MONEY) {
            // presupune ca a zis adevarul
            boolean fair = true;
            int penaltyPlayer = 0;
            int penaltySheriff = 0;

            for (int currGoodId : player.getInBag()) {
                // daca gaseste in sac un bun ilegal sau diferit de ce a declarat
                if (currGoodId != player.getDeclaredGood() || currGoodId > MAX_LEGALGOOD) {
                    // jucatorul a mintit si i se adauga la penalizare penalizarea bunului
                    fair = false;
                    penaltyPlayer +=
                            GoodsFactory.getInstance().getGoodsById(currGoodId).getPenalty();

                    // adaug bunurile confiscate la finalul pachetului
                    allGoodsId.add(currGoodId);
                } else {
                    // daca a spus adevarul adauga bunul pe taraba
                    player.addGoodInTable(currGoodId);
                }
            }

            // calculez ce penalty isi ia basic daca jucatorul a zis adevarul
            penaltySheriff +=
                    (GoodsFactory.getInstance().getGoodsById(player.getDeclaredGood()).getPenalty()
                            * player.getInBag().size());

            if (fair) {
                // daca a zis adevarul basic pierde bani care se adauga la jucator verificat
                this.updateMoney(-penaltySheriff);
                player.updateMoney(penaltySheriff);
            } else {
                // altfel basic castiga din banii pe care jucatorul ii pierde la penalizare
                this.updateMoney(penaltyPlayer);
                player.updateMoney(-penaltyPlayer);
                // iar jucatorul isi ia mita inapoi
                player.updateMoney(player.getBribe());
            }
        } else {
            /*  daca basic nu isi permite verificarea jucatorului acesta isi pune toate bunurile
            din sac pe taraba si isi ia si mita inapoi
            */
            player.updateMoney(player.getBribe());

            for (int currGoodId : player.getInBag()) {
                player.addGoodInTable(currGoodId);
            }
        }

        // jucatorul arde cartile ramase in sac
        player.discardBag();
    }
}
