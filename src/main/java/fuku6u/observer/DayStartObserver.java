package fuku6u.observer;

import fuku6u.Expectation.Parameter;
import fuku6u.Expectation.PossessedExpectation;
import fuku6u.Expectation.WolfGroupExpectation;
import fuku6u.board.BoardSurface;
import fuku6u.player.Utterance;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;

import java.util.Map;

public class DayStartObserver extends Observer {

    GameInfo gameInfo;
    BoardSurface boardSurface;
    WolfGroupExpectation wExpect;
    PossessedExpectation pExpect;

    public DayStartObserver(GameInfo gameInfo, BoardSurface boardSurface, WolfGroupExpectation wExpect, PossessedExpectation pExpect) {
        this.gameInfo = gameInfo;
        this.boardSurface = boardSurface;
        this.wExpect = wExpect;
        this.pExpect = pExpect;
    }

    public void check(Agent attackedAgent) {
        // 襲撃されたプレイヤは人狼グループにいない => グループから削除
        wExpect.clearAgent(attackedAgent);
        // 占い師が黒出ししたプレイヤが襲撃された =>　占い師は偽物　=> 狂狼の可能性が高い（特に狂人）（人狼がやる行動ではないがプロトコル部門ではあり得るのでは）
        for (Agent seerCOAgent :
                boardSurface.getComingOutAgentList(Role.SEER)) {  // 占い師COしたエージェント
            for (Map.Entry<Agent, Species> divinedResult:
                    boardSurface.getDivinedResult(seerCOAgent).entrySet()) { // 占い結果
                if (divinedResult.getKey().equals(attackedAgent) && divinedResult.getValue().equals(Species.WEREWOLF)) {    // 襲撃されたプレイヤに対して黒判定を出していた場合
                    lieSeerAgentList.add(seerCOAgent);
                    wExpect.distrustCalc(seerCOAgent, Parameter.convictionPossessedWerewolf);   // 狂狼を確信
                    pExpect.distrustCalc(seerCOAgent, Parameter.convictionPossessedWerewolf);  // ほぼ狂もしかしたら狼を確信

                    Utterance.getInstance().offer(Topic.ESTIMATE, seerCOAgent, Role.WEREWOLF);  // 「狼だと思う」
                    Utterance.getInstance().offer(Topic.ESTIMATE, seerCOAgent, Role.POSSESSED);  // 「狂人だと思う」
                    Utterance.getInstance().offer(Topic.VOTE, seerCOAgent); // 「VOTE発言」
                }
            }
        }


    }
}
