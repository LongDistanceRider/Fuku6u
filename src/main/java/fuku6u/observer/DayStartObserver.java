package fuku6u.observer;

import fuku6u.board.BoardSurface;
import fuku6u.player.Utterance;
import fuku6u.possessedExpectation.PossessedExpectation;
import fuku6u.possessedExpectation.PossessedParameter;
import fuku6u.wolfGroupExpectation.WolfGroupExpectation;
import fuku6u.wolfGroupExpectation.WolfGroupParameter;
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
        wExpect.deleteGroup(attackedAgent);
        // 占い師が黒出ししたプレイヤが襲撃された =>　占い師は偽物　=> 狂狼の可能性が高い（特に狂人）（人狼がやる行動ではないがプロトコル部門ではあり得るのでは）
        for (Agent seerCOAgent :
                boardSurface.getComingOutAgentList(Role.SEER)) {  // 占い師COしたエージェント
            for (Map.Entry<Agent, Species> divinedResult:
                    boardSurface.getDivinedResult(seerCOAgent).entrySet()) { // 占い結果
                if (divinedResult.getKey().equals(attackedAgent) && divinedResult.getValue().equals(Species.WEREWOLF)) {    // 襲撃されたプレイヤに対して黒判定を出していた場合
                    lieSeerAgentList.add(seerCOAgent);
                    wExpect.agentDistrustCalc(seerCOAgent, WolfGroupParameter.getConviction_PoseWolf());   // 狂狼を確信
                    pExpect.addAgentSuspect(seerCOAgent, PossessedParameter.getConviction_pose_wolf());  // ほぼ狂もしかしたら狼を確信

                    Utterance.getInstance().offer(Topic.ESTIMATE, seerCOAgent, Role.WEREWOLF);  // 「狼だと思う」
                    Utterance.getInstance().offer(Topic.ESTIMATE, seerCOAgent, Role.POSSESSED);  // 「狂人だと思う」
                    Utterance.getInstance().offer(Topic.VOTE, seerCOAgent); // 「VOTE発言」
                    // 真占い師確定しているか　これはtalkEndObserverへ移行
                    if (checkGenuineSeer(boardSurface, seerCOAgent)) {  // 確定
                        boardSurface.getComingOutAgentList(Role.SEER).forEach(agent -> {
                            if (!agent.equals(seerCOAgent)) { // 偽物は削除
                                findGenuineSeer(boardSurface, wExpect, pExpect, agent);   // 真占い師処理
                            }
                        });
                    }
                }
            }
        }

        // 真占の確定チェック


        // TODO 占霊狩COしたプレイヤは1人か（自身が占霊狩の場合は除く）
    }
}
