package fuku6u.observer;

import fuku6u.Expectation.Parameter;
import fuku6u.Expectation.PossessedExpectation;
import fuku6u.Expectation.WolfGroupExpectation;
import fuku6u.board.BoardSurface;
import fuku6u.utterance.Utterance;
import org.aiwolf.client.lib.TalkType;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import java.util.List;
import java.util.Map;

public class TalkObserver extends Observer {

    public static void comingout(BoardSurface boardSurface, WolfGroupExpectation wExpect, PossessedExpectation pExpect, Agent submit, Role coRole) {
        // 対抗COが存在するかを確認（占霊狩）
        Role myRole = boardSurface.getAssignRole().getRole();
        if (myRole.equals(Role.SEER) || myRole.equals(Role.MEDIUM) || myRole.equals(Role.BODYGUARD)) {
            if (myRole.equals(coRole)) {    // 対抗発見 => 狂狼を確信
                wExpect.distrustCalc(submit, Parameter.convictionPossessedWerewolf);
                pExpect.distrustCalc(submit, Parameter.convictionPossessedWerewolf);
                Utterance.getInstance().offer(Topic.ESTIMATE, submit, Role.POSSESSED, submit + "は偽物だよ！騙されないで！");  // 「狂人だと思う」
                Utterance.getInstance().offer(Topic.ESTIMATE, submit, Role.WEREWOLF, "");   // 「人狼だと思う」
                Utterance.getInstance().offer(Topic.VOTE, submit, submit + "はボク視点で偽物確定だから投票候補だね。");  // 「submitに投票する」
            }
        }
        // TODO 狂人COと人狼COがあった場合にPP発生を観測 => 役職クラスのPPフラグを立て，カミングアウト処理
    }


    public static void divined(BoardSurface boardSurface, WolfGroupExpectation wExpect, PossessedExpectation pExpect, Agent seerAgent, Agent target, Species result) {
        // 自分自身に黒出ししてきた => 占い師は偽物（人間から見ると）狂狼の可能性
        // 自分自身に白出ししてきた => 白より（人狼である可能性が少し低くなった程度であり，真狂狼の可能性は残る）
        if (boardSurface.getAssignRole().getRole() != Role.WEREWOLF) {  // 自分が人狼ではない場合
            if (target.equals(boardSurface.getMe())) {  // 対象が自分
                if (result.equals(Species.WEREWOLF)) { // 自分に黒出しされた
                    wExpect.distrustCalc(seerAgent, Parameter.convictionPossessedWerewolf);    // 黒より
                    pExpect.distrustCalc(seerAgent, Parameter.convictionPossessedWerewolf);    // 狂人の可能性を少しあげる
                    Utterance.getInstance().offer(Topic.ESTIMATE, seerAgent, Role.POSSESSED, "ボクは人間だよ！" + seerAgent + "は偽物だったんだ。");   //「狂人だと思う」
                    Utterance.getInstance().offer(Topic.VOTE, seerAgent, "ボクに黒出しした" + seerAgent + "は絶対偽物！みんな" + seerAgent + "に投票しよう！");   // 「VOTE発言」
                    addlieRoleAgentMapList(Role.SEER, seerAgent);   // 嘘つきをリストへ追加
                } else {    // 白だしされた
                    wExpect.distrustCalc(seerAgent, Parameter.unlikely);   // 白より
                    Utterance.getInstance().offer(Topic.ESTIMATE, seerAgent, Role.SEER, "ボクに白出しした" + seerAgent + "は少し真っぽいかな。");    //「占い師だと思う」
                }
            }
        } else {    // 自分が人狼である場合 => 占い師は（ほぼ）真　かつ　他の占い師は狂人 => (グループから削除) PosessedExpectationに処理を送る
            // memo: グループからの削除はしない　自分が人狼だと知った上での人狼グループ予想であるため，また，人狼役職の時の人狼予想は意味がない
            if (target.equals(boardSurface.getMe()) || boardSurface.getWerewolfList().contains(target)) {   // 自分または仲間の人狼に判定結果
                if (result.equals(Species.WEREWOLF)) { // 黒判定
                    // 狂人の可能性を大きく下げる
                    pExpect.distrustCalc(seerAgent, Parameter.veryTrust);
                    // 狂人特定
                    List<Agent> seerCoList = boardSurface.getComingOutAgentList(Role.SEER);
                    seerCoList.remove(seerAgent);
                    seerCoList.removeAll(boardSurface.getWerewolfList());
                    if (seerCoList.size() == 1) {   // ほぼ狂人
                        pExpect.distrustCalc(seerCoList.get(0), Parameter.convictionPossessedWerewolf);
                    }
                    // 対象が自分の場合は発言する
                    if (target.equals(boardSurface.getMe())) {
                        Utterance.getInstance().offer(Topic.ESTIMATE, seerAgent, Role.POSSESSED, seerAgent + "！君は真占い師だと思ってたのに……。偽物なんだね。"); // 「狂人だと思う」
                        Utterance.getInstance().offer(Topic.VOTE, seerAgent,"残念だけど" + seerAgent + "に投票するしかないかな。");   // 「VOTE発言」
                    }

                } else {    // 白判定
                    // 狂人を確信（真占い師が人狼に白出ししないと思っている）
                    pExpect.convictionAgent(seerAgent);
                    // 真占い師を特定
                    List<Agent> seerCoList = boardSurface.getComingOutAgentList(Role.SEER);
                    seerCoList.remove(seerAgent);
                    seerCoList.removeAll(boardSurface.getWerewolfList());
                    if (seerCoList.size() == 1) {
                        pExpect.distrustCalc(seerCoList.get(0), Parameter.veryTrust);
                    }
                    // 対象が自分の場合は発言する
                    if (target.equals(boardSurface.getMe())) {
                        Utterance.getInstance().offer(Topic.ESTIMATE, seerAgent, Role.SEER, "ボクに白出ししたのは真っぽいよね。");    //「占い師だと思う」
                    }
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

    public static void vote(BoardSurface boardSurface, int day, int id, Agent submit, Agent target) {
        // 自分に投票発言をしたか
        if (target.equals(boardSurface.getMe())) {
            Utterance.getInstance().offer(Topic.DISAGREE, TalkType.TALK, day, id, ">>" + submit + " " + "ちょっと待ってよ。ボクより怪しい人いるよ！");
        }
        // 黒出ししたAgentに投票発言したか
        if (boardSurface.getAssignRole().getBlackAgentList().contains(target)) {
                Utterance.getInstance().offer(Topic.AGREE, TalkType.TALK, day, id, ">>" + submit + " " + submit + "に賛成！" + target + "に投票しよう");
        }
        // 白出ししたAgentに投票発言したか
        if (boardSurface.getAssignRole().getBlackAgentList().contains(target)) {
            Utterance.getInstance().offer(Topic.DISAGREE, TalkType.TALK, day, id, ">>" + submit + " " + target + "は白だよ！");
        }
        // 占い師結果で白がわかっている人に投票しようとしているかを確認
        if (boardSurface.getAssignRole().getRole().equals(Role.SEER) &&
                boardSurface.getAssignRole().getResultWhiteAgentList().contains(target)) {
            Utterance.getInstance().offer(Topic.DISAGREE, TalkType.TALK, day, id, ">>" + submit + " " + target + "に投票かぁ。ボク視点では白っぽいんだよね。");
        }
    }
}
