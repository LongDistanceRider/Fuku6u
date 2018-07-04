package fuku6u.role;

import fuku6u.Expectation.WolfGroupExpectation;
import fuku6u.board.BoardSurface;
import fuku6u.board.Util;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

import java.util.List;

/**
 * 役職固有クラスの継承元クラス
 */
public abstract class AbstractRole {

    public abstract  Role getRole();

    public abstract void dayStart(GameInfo gameInfo, BoardSurface bs, WolfGroupExpectation wExpect);

    public abstract  void talk(BoardSurface boardSurface);

    public abstract void finish(BoardSurface boardSurface);

    /**
     * vote()処理
     * 村人側陣営の処理をデフォルトとしてこのクラスに書く
     * 人狼陣営など，処理を変更する場合はオーバーライドする
     *
     * @return 疑い度の高いエージェントを返す．
     * 同数の場合は複数のエージェントを返す
     * 全てのエージェントの疑い度が0の場合はnullが返却される
     */
    public List<Agent> vote(List<Agent> candidateAgentList, WolfGroupExpectation wExpect) {
        int maxDistrust = 0;
        List<Agent> mostDistrustAgentList = null;
        for (Agent agent :
                candidateAgentList) {
            int distrust = wExpect.getAgentDistrust(agent);
            if (distrust > maxDistrust) {
                mostDistrustAgentList.clear();
                maxDistrust = distrust;
            }
            if (distrust == maxDistrust) {
                mostDistrustAgentList.add(agent);
            }
        }
        return mostDistrustAgentList;
    }
}
