package fuku6u.role;

import fuku6u.Expectation.PossessedExpectation;
import fuku6u.Expectation.WolfGroupExpectation;
import fuku6u.board.BoardSurface;
import fuku6u.board.Util;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 役職固有クラスの継承元クラス
 */
public abstract class AbstractRole {

    /* PP発生 */
    protected boolean isPP = false;
    // 占い結果
    protected Map<Agent, Species> resultMap = new HashMap<>();
    // 黒出ししたエージェント
    protected List<Agent> blackAgentList = new ArrayList<>();
    // 白出ししたエージェント
    protected List<Agent> whiteAgentList = new ArrayList<>();

    public List<Agent> getBlackAgentList() {
        return blackAgentList;
    }

    public abstract  Role getRole();

    public abstract void dayStart(GameInfo gameInfo, BoardSurface bs, WolfGroupExpectation wExpect, PossessedExpectation pExpect);

    public abstract  void talk(BoardSurface boardSurface);

    public abstract void finish(BoardSurface boardSurface);

    /**
     * 占い結果で白が出ているAgentのリストを返す
     */
    public List<Agent> getResultWhiteAgentList() {
        List<Agent> resultWhiteAgentList = new ArrayList<>();
        resultMap.forEach(((agent, species) -> {
            if (species.equals(Species.HUMAN)) {
                resultWhiteAgentList.add(agent);
            }
        }));
        return resultWhiteAgentList;
    }
    /**
     * vote()処理
     * 村人側陣営の処理をデフォルトとしてこのクラスに書く
     * 人狼陣営など，処理を変更する場合はオーバーライドする
     *
     * @return 疑い度の高いエージェントを返す．
     * 同数の場合は複数のエージェントを返す
     * 全てのエージェントの疑い度が0の場合はnullが返却される
     */
    public List<Agent> vote(int day, BoardSurface boardSurface, List<Agent> candidateAgentList, WolfGroupExpectation wExpect, PossessedExpectation pExpect) {
        // 人狼の可能性が高いエージェントを返す　
        List<Agent> maxDistrustAgent = wExpect.getMaxDistrustAgent(candidateAgentList);
        if (!maxDistrustAgent.isEmpty()) {
            return maxDistrustAgent;
        }
        maxDistrustAgent = pExpect.getMaxDistrustAgent(candidateAgentList);
        if (!maxDistrustAgent.isEmpty()) {
            return maxDistrustAgent;
        }
        // 人狼の可能性がない場合は，狂人の可能性が高いエージェントを返す
        return candidateAgentList;
    }
}
