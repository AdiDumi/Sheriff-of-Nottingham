package com.tema1.main;

import com.tema1.Players.Player;
import com.tema1.Players.PlayerFactory;
import com.tema1.goods.Goods;
import com.tema1.goods.GoodsFactory;
import com.tema1.goods.IllegalGoods;
import com.tema1.goods.LegalGoods;
import com.tema1.common.PlayerComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.tema1.common.Constants.MAX_LEGALGOOD;


public final class Game {

    private Game() {
    }

    // desfasurarea jocului
    public static void playGame(final GameInput gameInput) {
        List<Integer> allGoodsId = gameInput.getAssetIds();
        ArrayList<Player> allPlayers = new ArrayList<>();
        List<String> playersNames = gameInput.getPlayerNames();

        // retin intr-un arraylist toti jucatorii cu strategia fiecaruia
        for (int i = 0; i < playersNames.size(); ++i) {
            allPlayers.add(PlayerFactory.getPlayer(playersNames.get(i), i));
        }

        int rounds = gameInput.getRounds();

        for (int i = 1; i <= rounds; ++i) {
            for (int j = 0; j < allPlayers.size(); ++j) {
                // fiecare player e serif pe rand
                Player playerSheriff = allPlayers.get(j);

                // toti jucatorii care nu sunt serifi trag bunuri in mana
                for (Player currPlayer : allPlayers) {
                    if (!currPlayer.equals(playerSheriff)) {
                        currPlayer.drawGoods(allGoodsId);
                        currPlayer.isVendor(i);
                    }
                }
                // seriful ii verifica pe toti
                playerSheriff.isSheriff(allPlayers, allGoodsId);

                // se reseteaza mita fiecarui jucator
                for (Player currPlayer : allPlayers) {
                    currPlayer.clearBribe();
                }
            }
        }

        // adaug bunurile legale care vin ca bonus la bunurile ilegale ale fiecarui jucator
        for (Player currPlayer : allPlayers) {
            for (int j = 0;  j < currPlayer.getInTable().size(); ++j) {
                if (currPlayer.getInTable().get(j) > MAX_LEGALGOOD) {
                    // daca bunul este ilegal obtin un map cu ce bunuri legale contine si freq lor
                    IllegalGoods illegalGood =
                            (IllegalGoods) GoodsFactory.getInstance().
                                    getGoodsById(currPlayer.getInTable().get(j));
                    Map<Goods, Integer> illegalMap = illegalGood.getIllegalBonus();
                    Set<Map.Entry<Goods, Integer>> illegalMapEntry = illegalMap.entrySet();

                    for (Map.Entry<Goods, Integer> currIllegalGood : illegalMapEntry) {
                        for (int i = 0;  i < currIllegalGood.getValue(); ++i) {
                            // adaug la taraba jucatorului bunul legal gasit cu frecventa lui
                            currPlayer.addGoodInTable(currIllegalGood.getKey().getId());
                        }
                    }
                }
            }
        }

        // adun pentru fiecare jucator la suma de bani profitul din bunurile de pe taraba
        int goodProfit;
        for (Player currPlayer : allPlayers) {
            for (int currGoodInTable : currPlayer.getInTable()) {
                goodProfit = GoodsFactory.getInstance().getGoodsById(currGoodInTable).getProfit();
                currPlayer.updateMoney(goodProfit);
            }
        }

        // pentru fiecare bun calculez cine primeste bonusul de King si Queen, daca se da
        int bonusKing, idKing;
        int bonusQueen, idQueen;
        int freqGood, freqKing, freqQueen;
        for (int i = 0; i < MAX_LEGALGOOD; ++i) {
            bonusKing = 0;
            bonusQueen = 0;
            idKing = -1;
            idQueen = -1;
            freqKing = 0;
            freqQueen = 0;
            LegalGoods legalGood = (LegalGoods) GoodsFactory.getInstance().getGoodsById(i);
            // caut acel bun la fiecare jucator sa vad ce frecventa are
            for (Player currPlayer : allPlayers) {
                freqGood = 0;
                for (int currGoodInTable : currPlayer.getInTable()) {
                    if (currGoodInTable == i) {
                        freqGood++;
                    }
                }
                /* daca l-am gasit macar odata il compar cu frecventa de King si Queen si
                retin id-ul jucatorului care detine king si celui care detine queen
                 */
                if (freqGood != 0) {
                    bonusKing = legalGood.getKingBonus();
                    bonusQueen = legalGood.getQueenBonus();
                    if (freqGood > freqKing) {
                        idQueen = idKing;
                        freqQueen = freqKing;
                        idKing = currPlayer.getId();
                        freqKing = freqGood;
                    } else {
                        // la frecvente egale primeste bonus cel cu id mai mic
                        if (freqGood == freqKing) {
                            if (currPlayer.getId() < idKing) {
                                idQueen = idKing;
                                freqQueen = freqKing;
                                idKing = currPlayer.getId();
                            } else {
                                if (freqGood > freqQueen) {
                                    idQueen = currPlayer.getId();
                                    freqQueen = freqGood;
                                } else {
                                    if (currPlayer.getId() < idQueen || idQueen == -1) {
                                        idQueen = currPlayer.getId();
                                        freqQueen = freqGood;
                                    }
                                }
                            }
                        } else {
                            if (idQueen == -1) {
                                idQueen = currPlayer.getId();
                                freqQueen = freqGood;
                            } else {
                                if (freqGood > freqQueen) {
                                    idQueen = currPlayer.getId();
                                    freqQueen = freqGood;
                                }
                            }
                        }
                    }
                }
            }

            // adaug bonusul de king si queen al fiecarui bun la jucatorul obtinut mai sus
            for (Player currPlayer : allPlayers) {
                if (currPlayer.getId() == idKing) {
                    currPlayer.updateMoney(bonusKing);
                }
                if (currPlayer.getId() == idQueen) {
                    currPlayer.updateMoney(bonusQueen);
                }
            }
        }

        // sortez jucatorii dupa suma de bani totala si la egalitate cel cu id-ul mai mic
        PlayerComparator playerComparator = new PlayerComparator();
        Collections.sort(allPlayers, playerComparator);

        // afisez jucatorii sortati corespunzator
        for (Player currPlayer : allPlayers) {
            System.out.println(currPlayer.getId() + " " + currPlayer.getType().toUpperCase() + " "
                    + currPlayer.getTotalMoney());
        }
    }
}
