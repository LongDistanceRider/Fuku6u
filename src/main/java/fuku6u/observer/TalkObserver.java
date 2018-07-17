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

import java.util.List;

public class TalkObserver extends Observer {

    public static void comingout(BoardSurface boardSurface, WolfGroupExpectation wExpect, PossessedExpectation pExpect, Agent submit, Role coRole) {
        // 対抗COが存在するかを確認（占霊狩）
        Role myRole = boardSurface.getAssignRole().getRole();
        if (myRole.equals(Role.SEER) || myRole.equals(Role.MEDIUM) || myRole.equals(Role.BODYGUARD)) {
            if (myRole.equals(coRole)) {    // 対抗発見 => 狂狼を確信
                wExpect.distrustCalc(submit, Parameter.convictionPossessedWerewolf);
                pExpect.distrustCalc(submit, Parameter.convictionPossessedWerewolf);
                Utterance.getInstance().offer(Topic.ESTIMATE, submit, Role.POSSESSED);  // 「狂人だと思う」
                Utterance.getInstance().offer(Topic.ESTIMATE, submit, Role.WEREWOLF);   // 「人狼だと思う」
                Utterance.getInstance().offer(Topic.VOTE, submit);  // 「submitに投票する」
            }
        }
    }


    public static void divined(BoardSurface boardSurface, WolfGroupExpectation wExpect, PossessedExpectation pExpect, Agent seerAgent, Agent target, Species result) {
        // 自分自身に黒出ししてきた => 占い師は偽物（人間から見ると）狂狼の可能性
        // 自分自身に白出ししてきた => 白より（人狼である可能性が少し低くなった程度であり，真狂狼の可能性は残る）
        if (boardSurface.getAssignRole().getRole() != Role.WEREWOLF) {  // 自分が人狼ではない場合
            if (target.equals(boardSurface.getMe())) {  // 対象が自分
                if (result.equals(Species.WEREWOLF)) { // 自分に黒出しされた
                    wExpect.distrustCalc(seerAgent, Parameter.convictionPossessedWerewolf);    // 黒より
                    pExpect.distrustCalc(seerAgent, Parameter.convictionPossessedWerewolf);    // 狂人の可能性を少しあげる
                    Utterance.getInstance().offer(Topic.ESTIMATE, seerAgent, Role.POSSESSED);   //「狂人だと思う」
                    Utterance.getInstance().offer(Topic.VOTE, seerAgent);   // 「VOTE発言」
                    addlieRoleAgentMapList(Role.SEER, seerAgent);   // 嘘つきをリストへ追加
                } else {    // 白だしされた
                    wExpect.distrustCalc(seerAgent, Parameter.unlikely);   // 白より
                    Utterance.getInstance().offer(Topic.ESTIMATE, seerAgent, Role.SEER);    //「占い師だと思う」
                }
            }
        } else {    // 自分が人狼である場合 => 占い師は真　かつ　他の占い師は狂人 => (グループから削除) PosessedExpectationに処理を送る
            // memo: グループからの削除はしない　自分が人狼だと知った上での人狼グループ予想であるため，また，人狼役職の時の人狼予想は意味がない
            // 占い師COしたエージェントリストを取得
            if (target.equals(boardSurface.getMe())) { // 対象が自分
                if (result.equals(Species.WEREWOLF)) {  // 自分に黒出しした
                    List<Agent> seerCOList = boardSurface.getComingOutAgentList(Role.SEER);
                    seerCOList.remove(seerAgent);   // 真占い師をリムーブ
                    seerCOList.remove(boardSurface.getWerewolfList());

                    Utterance.getInstance().offer(Topic.ESTIMATE, seerAgent, Role.POSSESSED); // 「狂人だと思う」
                    Utterance.getInstance().offer(Topic.VOTE, seerAgent);   // 「VOTE発言」
                    if (seerCOList.size() == 1) {
                        pExpect.convictionAgent(seerCOList.get(0));
                    }
                } else {    // 自分に白出しした
                    if (!boardSurface.getWerewolfList().contains(seerAgent)) {
                        pExpect.convictionAgent(seerAgent);
                    }
                    Utterance.getInstance().offer(Topic.ESTIMATE, seerAgent, Role.SEER);    //「占い師だと思う」
                }
            }
        }
        // 白を出されたエージェント（自分以外）は白寄りに
        if (!target.equals(boardSurface.getMe())) {
            if (result.equals(Species.HUMAN)) {
                wExpect.distrustCalc(target, Parameter.unlikely);
            } else {
                // 黒を出されたエージェントは黒寄りに
                wExpect.distrustCalc(target, Parameter.likely);
            }
        }
    }
    public static void identified(WolfGroupExpectation wExpect, Agent target, Species result) {
        // 白を出されたエージェントは白よりに
        if (result.equals(Species.HUMAN)) {
            wExpect.distrustCalc(target, Parameter.unlikely);
        } else {
            // 黒を出されたエージェントは黒寄りに
            wExpect.distrustCalc(target, Parameter.likely);
        }
    }
}
