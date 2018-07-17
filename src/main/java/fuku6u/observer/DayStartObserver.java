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

import javax.management.relation.RoleStatus;
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
                    // 嘘つきをリスト追加
                    addlieRoleAgentMapList(Role.SEER, seerCOAgent);
                    wExpect.distrustCalc(seerCOAgent, Parameter.convictionPossessedWerewolf);   // 狂狼を確信
                    pExpect.distrustCalc(seerCOAgent, Parameter.convictionPossessedWerewolf);  // ほぼ狂もしかしたら狼を確信

                    Utterance.getInstance().offer(Topic.ESTIMATE, seerCOAgent, Role.WEREWOLF);  // 「狼だと思う」
                    Utterance.getInstance().offer(Topic.ESTIMATE, seerCOAgent, Role.POSSESSED);  // 「狂人だと思う」
                    Utterance.getInstance().offer(Topic.VOTE, seerCOAgent); // 「VOTE発言」
                }
            }
        }
        // 自分の役職が霊能者の場合，結果を受けて人狼予想をする．また，黒出しされたプレイヤが占い師COしている場合は，全ての占い結果をバックトラックする．
        if (boardSurface.getAssignRole().getRole().equals(Role.MEDIUM)) {
            // 判定によって人狼グループ予想クラスの処理をする
            Map<Agent, Species> mediumResultMap = boardSurface.getMediumResultMap();    // 自分自身の霊能結果
            mediumResultMap.forEach(((agent, species) -> {
                if (species.equals(Species.HUMAN)) {
                    wExpect.clearAgent(agent);
                } else {
                    wExpect.convictionAgent(agent);
                    // 占い師COしていたか
                    if (boardSurface.getCoRole(agent).equals(Role.SEER)) {
                        // 偽物確定
                        addlieRoleAgentMapList(Role.SEER, agent);
                        // 発言した占い結果による影響をバックトラック
                        Map<Agent, Species> lieSeerDivinedResult = boardSurface.getDivinedResult(agent);
                        lieSeerDivinedResult.forEach((target_lie, species_lie) -> {
                            // 白を出されたエージェント（自分以外）は白寄りに なっているため，これを逆算
                            if (!target_lie.equals(boardSurface.getMe())) {
                                if (species_lie.equals(Species.HUMAN)) {
                                    wExpect.distrustCalc(target_lie, Parameter.likely);
                                } else {
                                    // 黒を出されたエージェントは黒寄りに　なっているため，これを逆算
                                    wExpect.distrustCalc(target_lie, Parameter.unlikely);
                                }
                            }
                        });
                    }
                }
            }));
        }
        // 自分の役職が占い師の場合，結果を受けて人狼予想をする．また，黒出しされたプレイヤが霊能者COしている場合は，全ての霊能結果をバックトラックする．
        if (boardSurface.getAssignRole().getRole().equals(Role.SEER)) {
            Map<Agent, Species> divinedResultMap = boardSurface.getMediumResultMap(); // 自分自身の霊能結果
            divinedResultMap.forEach((agent, species) -> {
                if (species.equals(Species.HUMAN)) {
                    wExpect.clearAgent(agent);
                } else {
                    wExpect.convictionAgent(agent);
                    // 霊能COしていたか
                    if (boardSurface.getCoRole(agent).equals(Role.MEDIUM)) {
                        // 偽物確定
                        addlieRoleAgentMapList(Role.MEDIUM, agent);
                        // 発言した霊能結果による影響をバックトラック
                        Map<Agent, Species> lieIdentifiedResult = boardSurface.getIdenResult(agent);
                        lieIdentifiedResult.forEach((target_lie, species_lie) -> {
                            // 白を出されたエージェント（自分以外）は白寄りに なっているため，これを逆算
                            if (!target_lie.equals(boardSurface.getMe())) {
                                if (species_lie.equals(Species.HUMAN)) {
                                    wExpect.distrustCalc(target_lie, Parameter.likely);
                                } else {
                                    // 黒を出されたエージェントは黒寄りに　なっているため，これを逆算
                                    wExpect.distrustCalc(target_lie, Parameter.unlikely);
                                }
                            }
                        });
                    }
                }
            });
        }


    }
}
