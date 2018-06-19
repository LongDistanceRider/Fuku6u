package fuku6u.posessedExpectation;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 狂人予想
 */
public class PosessedExpectation {

    /* 狂人疑い */
    Map<Agent, Integer> posessedSuspectMap = new HashMap<>();

    public PosessedExpectation(GameInfo gameInfo) {
        List<Agent> agentList = gameInfo.getAliveAgentList();   // 参加エージェント
        agentList.remove(gameInfo.getAgent()); // 自分自身は除く
        agentList.forEach(agent -> posessedSuspectMap.put(agent, 0));  // 狂人疑いマップを更新
    }

    /**
     * エージェントの疑い度を更新
     * @param agent
     *  更新するエージェント
     * @param addDistrust
     *  追加する疑い度
     */
    public void agentSispectCalc (Agent agent, int addDistrust) {
        int preDistrust = posessedSuspectMap.get(agent);
        int distrust = preDistrust + addDistrust;
        posessedSuspectMap.put(agent, distrust);
    }
}
