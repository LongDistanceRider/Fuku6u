package fuku6u.observation;

import fuku6u.board.BoardSurface;
import fuku6u.log.Log;
import fuku6u.posessedExpectation.PosessedExpectation;
import fuku6u.posessedExpectation.PosessedParameter;
import fuku6u.wolfGroupExpectation.WolfGroupExpectation;
import fuku6u.wolfGroupExpectation.WolfGroupParameter;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import java.util.List;
import java.util.Map;

/**
 * ある地点において観測すべき状態をまとめたクラス
 *  ex: TalkProcessingにてComingout処理をした際に，自分と対抗であった場合，黒判定をBoardSurfaceとWolfGroupExpectationに送る
 *      襲撃者がわかった時点で黒ではないことが確定するため，WolfGroupExpectationに予想グループから外すように処理を送る
 */
public class Observation {

    /**
     * 観測箇所: dayStart()の終わりに呼び出される
     * 観測対象: 襲撃されたプレイヤ
     * 処理対象: 襲撃されたプレイヤは人狼グループにいない
     *          襲撃されたプレイヤは黒出しされていたか
     *
     * @param attackedAgent
     *  襲撃されたエージェント
     */
    public static void dayStart(BoardSurface bs, WolfGroupExpectation wExpect, PosessedExpectation pExpect, Agent attackedAgent) {
        // 襲撃されたプレイヤは人狼グループにいない => グループから削除
        wExpect.deleteGroup(attackedAgent);
        // 占い師が黒出ししたプレイヤが襲撃された =>　占い師は偽物　=> 狂狼の可能性が高い（特に狂人）（人狼がやる行動ではないがプロトコル部門ではあり得るのでは）
        for (Agent seerCOAgent :
                bs.getComingOutAgentList(Role.SEER)) {  // 占い師COしたエージェント
            for (Map.Entry<Agent, Species> divinedResult:
                 bs.getDivinedResult(seerCOAgent).entrySet()) { // 占い結果
                if (divinedResult.getKey().equals(attackedAgent) && divinedResult.getValue().equals(Species.WEREWOLF)) {    // 襲撃されたプレイヤに対して黒判定を出していた場合
                    wExpect.agentDistrustCalc(seerCOAgent, WolfGroupParameter.getConviction_PoseWolf());   // 狂狼を確信
                    pExpect.addAgentSuspect(seerCOAgent, PosessedParameter.getConviction_pose_wolf());  // ほぼ狂もしかしたら狼を確信
                }
            }
        }
    }

    /**
     * 観測箇所: TalkProcessingのDIVINED発言があった後に呼び出される
     * 観測対象: 発言した占い師COエージェント（その他の占い師COしたエージェント）
     * 処理対象: 自分自身に出した判定によって人狼グループ予想をする
     *          自身が人狼の場合，黒出しされたら，真占い師の可能性があるため，その他の占い師COエージェントの狂人確率を少し上げる（誤爆の可能性を考慮する）
     * @param seerAgent
     */
    public static void divined(BoardSurface bs, WolfGroupExpectation wExpect, PosessedExpectation pExpect, Agent seerAgent, Agent target, Species result) {
        // 自分自身に黒出ししてきた => 占い師は偽物（人間から見ると）狂狼の可能性
        // 自分自身に白出ししてきた => 白より（人狼である可能性が少し低くなった程度であり，真狂狼の可能性は残る）
        if (bs.getAssignRole().getRole() != Role.WEREWOLF) {  // 自分が人狼ではない場合
            if (target.equals(bs.getMe())) {  // 対象が自分
                if (result.equals(Species.WEREWOLF)) { // 自分に黒出しされた
                    wExpect.agentDistrustCalc(seerAgent, WolfGroupParameter.getConviction_PoseWolf());    // 黒より
                    pExpect.addAgentSuspect(seerAgent, PosessedParameter.getMay_pose());    // 狂人の可能性を少しあげる
                } else {    // 白だしされた
                    wExpect.agentDistrustCalc(seerAgent, WolfGroupParameter.getUnlikely_Wolf());   // 白より
                }
            }
        } else {    // 自分が人狼である場合 => 占い師は真　かつ　他の占い師は狂人 => (グループから削除) PosessedExpectationに処理を送る
            // memo: グループからの削除はしない　自分が人狼だと知った上での人狼グループ予想であるため，また，人狼役職の時の人狼予想は意味がない
            // 占い師COしたエージェントリストを取得
            List<Agent> seerCOList = bs.getComingOutAgentList(Role.SEER);
            seerCOList.remove(seerAgent);   // 真占い師をリムーブ
            if (!seerCOList.isEmpty()) {
                seerCOList.forEach(seerCOAgent -> pExpect.addAgentSuspect(seerCOAgent, PosessedParameter.getConviction_pose()));
            }
        }
        // 白を出されたエージェントは白寄りに
        if (result.equals(Species.HUMAN)) {
            wExpect.agentDistrustCalc(target, WolfGroupParameter.getLikely_White());
        } else {
            // 黒を出されたエージェントは黒寄りに
            wExpect.agentDistrustCalc(target, WolfGroupParameter.getLikely_Black());
        }
    }

    /**
     * 観測箇所: TalkProcessingのCOMINGOUT発言があった場合に呼び出される
     * 観測対象: 発言したエージェントと役職
     * 処理対象: 占い師CO・霊能COしたエージェントが1人の場合は確定白としてグループから削除
     *          自身が占い師の場合，霊能者の場合は，対抗は狂狼を確信
     *          占い師または霊能が2人以上の場合は狂狼が混ざっているため，少し不信度をあげる？
     */
    public static void comingout() {

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
                Log.debug("想定していない進行を確認");
        }

    }
}
