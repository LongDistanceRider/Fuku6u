package fuku6u.observer;

import fuku6u.board.BoardSurface;
import fuku6u.player.Utterance;
import fuku6u.possessedExpectation.PossessedExpectation;
import fuku6u.possessedExpectation.PossessedParameter;
import fuku6u.wolfGroupExpectation.WolfGroupExpectation;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 盤面状態観測クラス
 *
 */
public abstract class Observer {

    /* 偽占COをしているエージェントを保管 */
    protected static List<Agent> lieSeerAgentList = new ArrayList<>();
    /* 偽霊COをしているエージェントを保管 */
    protected static List<Agent> lieMediumAgentList = new ArrayList<>();

    /**
     * 真占い師を観測した場合の処理をまとめる
     */
    protected static void findGenuineSeer(BoardSurface bs, WolfGroupExpectation wExpect, PossessedExpectation pExpect, Agent genuineSeer) {
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
    protected static boolean checkGenuineSeer(BoardSurface bs, Agent fakeSeer) {
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
