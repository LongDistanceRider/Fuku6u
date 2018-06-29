package fuku6u.observation;

import fuku6u.board.BoardSurface;
import fuku6u.log.Log;
import fuku6u.player.Utterance;
import fuku6u.possessedExpectation.PossessedExpectation;
import fuku6u.possessedExpectation.PossessedParameter;
import fuku6u.role.Seer;
import fuku6u.wolfGroupExpectation.WolfGroupExpectation;
import fuku6u.wolfGroupExpectation.WolfGroupParameter;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import java.util.List;
import java.util.Map;

/**
 * ある地点において観測すべき状態をまとめたクラス
 * 特定の状態の際にWolfGroupExceptionとPossessedExceptionに送る．
 * また，特定の発言を行う．
 *  ex: TalkProcessingにてComingout処理をした際に，自分と対抗であった場合WolfGroupExpectationに送る
 *      襲撃者がわかった時点で黒ではないことが確定するため，WolfGroupExpectationに予想グループから外すように処理を送る
 *
 * memo: プロダクションシステムのようなクラス
 *  ワーキングメモリをBoardSurfaceとして，条件部と行動部がメソッド内に書かれる
 */
public class Observation {

    // TODO 黒を確信したエージェントに対して白を出しているエージェントは偽物
    /**
     * 観測箇所: dayStart()の終わりに呼び出される
     * 観測対象: 襲撃されたプレイヤ
     * 処理対象: 襲撃されたプレイヤは人狼グループにいない
     *          襲撃されたプレイヤは黒出しされていたか
     *          占霊狩COしたプレイヤは1人か（自分の役職が占霊狩の場合は除く）
     *
     * @param attackedAgent
     *  襲撃されたエージェント
     */
    public static void dayStart(BoardSurface bs, WolfGroupExpectation wExpect, PossessedExpectation pExpect, Agent attackedAgent) {
        // 襲撃されたプレイヤは人狼グループにいない => グループから削除
        wExpect.deleteGroup(attackedAgent);
        // 占い師が黒出ししたプレイヤが襲撃された =>　占い師は偽物　=> 狂狼の可能性が高い（特に狂人）（人狼がやる行動ではないがプロトコル部門ではあり得るのでは）
        for (Agent seerCOAgent :
                bs.getComingOutAgentList(Role.SEER)) {  // 占い師COしたエージェント
            for (Map.Entry<Agent, Species> divinedResult:
                 bs.getDivinedResult(seerCOAgent).entrySet()) { // 占い結果
                if (divinedResult.getKey().equals(attackedAgent) && divinedResult.getValue().equals(Species.WEREWOLF)) {    // 襲撃されたプレイヤに対して黒判定を出していた場合
                    wExpect.agentDistrustCalc(seerCOAgent, WolfGroupParameter.getConviction_PoseWolf());   // 狂狼を確信
                    pExpect.addAgentSuspect(seerCOAgent, PossessedParameter.getConviction_pose_wolf());  // ほぼ狂もしかしたら狼を確信

                    Utterance.getInstance().offer(Topic.ESTIMATE, seerCOAgent, Role.WEREWOLF);  // 「狼だと思う」
                    Utterance.getInstance().offer(Topic.ESTIMATE, seerCOAgent, Role.POSSESSED);  // 「狂人だと思う」
                    Utterance.getInstance().offer(Topic.VOTE, seerCOAgent); // 「VOTE発言」
                    // 真占い師確定しているか
                    if (checkGenuineSeer(bs, seerCOAgent)) {  // 確定
                        bs.getComingOutAgentList(Role.SEER).forEach(agent -> {
                            if (!agent.equals(seerCOAgent)) { // 偽物は削除
                                findGenuineSeer(bs, wExpect, pExpect, agent);   // 真占い師処理
                            }
                        });
                    }
                }
            }
        }
        // TODO 占霊狩COしたプレイヤは1人か（自身が占霊狩の場合は除く）
    }

    /**
     * 観測箇所: TalkProcessingのDIVINED発言があった後に呼び出される
     * 観測対象: 発言した占い師COエージェント（その他の占い師COしたエージェント）
     * 処理対象: 自分自身に出した判定によって人狼グループ予想をする
     *          自身が人狼の場合，黒出しされたら，真占い師の可能性があるため，その他の占い師COエージェントの狂人確率を少し上げる（誤爆の可能性を考慮する）
     *          真占い師が確定した時点で，確定白と確定黒を把握し，グループ削除を行う
     * @param seerAgent
     */
    public static void divined(BoardSurface bs, WolfGroupExpectation wExpect, PossessedExpectation pExpect, Agent seerAgent, Agent target, Species result) {
        // 自分自身に黒出ししてきた => 占い師は偽物（人間から見ると）狂狼の可能性
        // 自分自身に白出ししてきた => 白より（人狼である可能性が少し低くなった程度であり，真狂狼の可能性は残る）
        if (bs.getAssignRole().getRole() != Role.WEREWOLF) {  // 自分が人狼ではない場合
            if (target.equals(bs.getMe())) {  // 対象が自分
                if (result.equals(Species.WEREWOLF)) { // 自分に黒出しされた
                    wExpect.agentDistrustCalc(seerAgent, WolfGroupParameter.getConviction_PoseWolf());    // 黒より
                    pExpect.addAgentSuspect(seerAgent, PossessedParameter.getMay_pose());    // 狂人の可能性を少しあげる
                    Utterance.getInstance().offer(Topic.ESTIMATE, seerAgent, Role.POSSESSED);   //「狂人だと思う」
                    Utterance.getInstance().offer(Topic.VOTE, seerAgent);   // 「VOTE発言」
                    // 真占い師確定しているか
                    if (checkGenuineSeer(bs, seerAgent)) {  // 確定
                        bs.getComingOutAgentList(Role.SEER).forEach(agent -> {
                            if (!agent.equals(seerAgent)) { // 偽物は削除
                                findGenuineSeer(bs, wExpect, pExpect, agent);   // 真占い師処理
                            }
                        });
                    }
                } else {    // 白だしされた
                    wExpect.agentDistrustCalc(seerAgent, WolfGroupParameter.getUnlikely_Wolf());   // 白より
                    Utterance.getInstance().offer(Topic.ESTIMATE, seerAgent, Role.SEER);    //「占い師だと思う」
                }
            }
        } else {    // 自分が人狼である場合 => 占い師は真　かつ　他の占い師は狂人 => (グループから削除) PosessedExpectationに処理を送る
            // memo: グループからの削除はしない　自分が人狼だと知った上での人狼グループ予想であるため，また，人狼役職の時の人狼予想は意味がない
            // 占い師COしたエージェントリストを取得
            if (target.equals(bs.getMe())) { // 対象が自分
                if (result.equals(Species.WEREWOLF)) {  // 自分に黒出しした
                    List<Agent> seerCOList = bs.getComingOutAgentList(Role.SEER);
                    seerCOList.remove(seerAgent);   // 真占い師をリムーブ
                    Utterance.getInstance().offer(Topic.ESTIMATE, seerAgent, Role.POSSESSED); // 「狂人だと思う」
                    Utterance.getInstance().offer(Topic.VOTE, seerAgent);   // 「VOTE発言」
                    if (!seerCOList.isEmpty()) {
                        seerCOList.forEach(seerCOAgent -> pExpect.addAgentSuspect(seerCOAgent, PossessedParameter.getConviction_pose()));
                    }
                } else {    // 自分に白出しした
                    pExpect.addAgentSuspect(seerAgent, PossessedParameter.getConviction_pose());
                    Utterance.getInstance().offer(Topic.ESTIMATE, seerAgent, Role.SEER);    //「占い師だと思う」
                }
            }
        }
        // 白を出されたエージェント（自分以外）は白寄りに
        if (!target.equals(bs.getMe())) {
            if (result.equals(Species.HUMAN)) {
                wExpect.agentDistrustCalc(target, WolfGroupParameter.getLikely_White());
            } else {
                // 黒を出されたエージェントは黒寄りに
                wExpect.agentDistrustCalc(target, WolfGroupParameter.getLikely_Black());
            }
        }
        // 
    }

    /**
     * 観測箇所: TalkProcessingのCOMINGOUT発言があった場合に呼び出される
     * 観測対象: 発言したエージェントと役職
     * 処理対象: 自身が占い師の場合，霊能者の場合は，対抗は狂狼を確信
     *          占い師または霊能が2人以上の場合は狂狼が混ざっているため，少し不信度をあげる？
     */
    public static void comingout(BoardSurface bs, WolfGroupExpectation wExpect, PossessedExpectation pExpect, Agent submit, Role coRole) {
        // 対抗COが存在するかを確認（占霊狩）
        Role myRole = bs.getAssignRole().getRole();
        if (myRole.equals(Role.SEER) || myRole.equals(Role.MEDIUM) || myRole.equals(Role.BODYGUARD)) {
            if (myRole.equals(coRole)) {    // 対抗発見 => 狂狼を確信
                wExpect.agentDistrustCalc(submit, WolfGroupParameter.getConviction_PoseWolf());
                pExpect.addAgentSuspect(submit, PossessedParameter.getConviction_pose_wolf());
                Utterance.getInstance().offer(Topic.ESTIMATE, submit, Role.POSSESSED);  // 「狂人だと思う」
                Utterance.getInstance().offer(Topic.ESTIMATE, submit, Role.WEREWOLF);   // 「人狼だと思う」
                Utterance.getInstance().offer(Topic.VOTE, submit);  // 「submitに投票する」
            }
        }
    }

    /**
     * 投票決定前にBoardSurfaceから取れる情報より状態を更新する（前提: 1日目に占い師と霊能者のCOが終わっている）
     * 観測箇所: vote()
     * 観測対象: BoardSurface
     * 処理対象: 確定白・確定黒が存在するか
     *          ○-○進行によって人狼グループの更新をかける
     */
    public static void vote(BoardSurface bs) {
        // TODO 確定白が存在する場合 => そのエージェントがいるグループは削除
        // TODO 確定黒が存在する場合 => そのエージェントがいるグループのみ残す
        // TODO ○-○進行によって処理を変える(後回し)
        int seerCONum = bs.getComingOutAgentList(Role.SEER).size();
        int mediumCONum = bs.getComingOutAgentList(Role.MEDIUM).size();
        String progress = seerCONum + "-" + mediumCONum;
        switch (progress) {
            case "0-0":
                break;
            case "0-1":
                break;
            case "1-0":
                break;
            case "1-1":
                break;
            case "1-2":
                break;
            case "1-3":
                break;
            default:
                Log.debug("想定していない進行を確認:" + progress);
        }

    }

    /**
     * 真占い師を観測した場合の処理をまとめる
     */
    private static void findGenuineSeer(BoardSurface bs, WolfGroupExpectation wExpect, PossessedExpectation pExpect, Agent genuineSeer) {
        Map<Agent, Species> divinedResultMap = bs.getDivinedResult(genuineSeer); // 真占い師が発言した占い結果を取得
        for (Map.Entry<Agent, Species> divEntry :
                divinedResultMap.entrySet()) {
            if (divEntry.getValue().equals(Species.WEREWOLF)) { // 黒出しエージェント発見
                wExpect.remainGroup(divEntry.getKey()); // 人狼確定
                pExpect.addAgentSuspect(divEntry.getKey(), PossessedParameter.getConviction_wolf());
                Utterance.getInstance().offer(Topic.ESTIMATE, divEntry.getKey(), Role.WEREWOLF);
                Utterance.getInstance().offer(Topic.VOTE, divEntry.getKey());
            } else {    // 白だしエージェント発見
                wExpect.deleteGroup(divEntry.getKey());
                Utterance.getInstance().offer(Topic.ESTIMATE, divEntry.getKey(), Role.VILLAGER);
            }
        }
        // TODO 偽占い師の判定結果をリセットする必要がある
    }

    /**
     * 真占い師が確定しているかをチェックする
     *
     * @param fakeSeer
     *
     * @return 自分自身が占い師である場合にはfalseを返す．
     *  真占い師が発見された場合のみtrueを返す．
     */
    private static boolean checkGenuineSeer(BoardSurface bs, Agent fakeSeer) {
        // TODO この書き方だと占い師3人いたチキに対応できない
        if (bs.getAssignRole().getRole().equals(Role.SEER)) {
            return false;
        }
        List<Agent> seerCOList = bs.getComingOutAgentList(Role.SEER);   // 占い師発言リスト
        seerCOList.remove(fakeSeer);
        if (seerCOList.size() == 1) {   // 1人しかいないなら真占確定
            return true;
        }
        return false;
    }
}
