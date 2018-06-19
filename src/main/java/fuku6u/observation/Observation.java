package fuku6u.observation;

import fuku6u.board.BoardSurface;
import fuku6u.posessedExpectation.PosessedExpectation;
import fuku6u.posessedExpectation.PosessedParameter;
import fuku6u.wolfGroupExpectation.WolfGroupExpectation;
import fuku6u.wolfGroupExpectation.WolfGroupParameter;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import java.util.List;

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
    public static void dayStart(WolfGroupExpectation wolfGroupExpectation, Agent attackedAgent) {
        // 襲撃されたプレイヤは人狼グループにいない => グループから削除
        wolfGroupExpectation.deleteGroup(attackedAgent);
        // 占い師が黒出ししたプレイヤが襲撃された =>　占い師は偽物
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
                    wExpect.agentDistrustCalc(seerAgent, WolfGroupParameter.getConviction_pose_were());    // 黒より
                    pExpect.agentSispectCalc(seerAgent, PosessedParameter.getMay_pose());    // 狂人の可能性を少しあげる
                } else {    // 白だしされた
                    wExpect.agentDistrustCalc(seerAgent, WolfGroupParameter.getUnlikely_were());   // 白より
                }
            }
        } else {    // 自分が人狼である場合 => 占い師は真　かつ　他の占い師は狂人 => (グループから削除) PosessedExpectationに処理を送る
            // memo: グループからの削除はしない　自分が人狼だと知った上での人狼グループ予想であるため，また，人狼役職の時の人狼予想は意味がない
            // 占い師COしたエージェントリストを取得
            List<Agent> seerCOList = bs.getComingOutAgentList(Role.SEER);
            seerCOList.remove(seerAgent);   // 真占い師をリムーブ
            if (!seerCOList.isEmpty()) {
                seerCOList.forEach(seerCOAgent -> pExpect.agentSispectCalc(seerCOAgent, PosessedParameter.getConviction_pose()));
            }
        }
    }
}
