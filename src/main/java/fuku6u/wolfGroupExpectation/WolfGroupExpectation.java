package fuku6u.wolfGroupExpectation;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WolfGroupExpectation {

    /* WolfGroupマップ */
    private Map<WolfGroup, Boolean> isWolfGroupMap = new HashMap<>();
    /* Agentの灰・黒可能性フラグ（各白はfalse） */ // TODO このリスト必要ないかも　要検討
    private Map<Agent, Boolean> isAgentDistrustMap = new HashMap<>();
    /* エージェントの不信度 */
    private Map<Agent, Integer> agentDistrustMap = new HashMap<>();

    /**
     * コンストラクタ
     * @param gameInfo
     */
    public WolfGroupExpectation(GameInfo gameInfo) {
        List<Agent> agentList = gameInfo.getAliveAgentList();   // 参加エージェント
        agentList.remove(gameInfo.getAgent()); // 自分自身は除く
        for (int i = 0; i < agentList.size(); i++) {
            for (int j = i+1; j < agentList.size(); j++) {
                for (int k = j+1; k < agentList.size(); k++) {
                    List<Agent> agents = new ArrayList<>(); // WolfGroupのメンバーを登録
                    agents.add(agentList.get(i));
                    agents.add(agentList.get(j));
                    agents.add(agentList.get(k));

                    WolfGroup wolfGroup = new WolfGroup(agents);    // WolfGroupの作成
                    isWolfGroupMap.put(wolfGroup, Boolean.TRUE);
                }
            }
            isAgentDistrustMap.put(agentList.get(i), Boolean.TRUE);   // フラグ初期化
            agentDistrustMap.put(agentList.get(i), 0);
        }
    }

    /**
     * エージェントの疑い度を更新
     * @param agent
     *  更新するエージェント
     * @param addDistrust
     *  追加する疑い度
     */
    public void agentDistrustCalc (Agent agent, int addDistrust) {
        int preDistrust = agentDistrustMap.get(agent);
        int distrust = preDistrust + addDistrust;
        agentDistrustMap.put(agent, distrust);
    }

    /**
     * 特定のエージェントがいるグループを削除(FALSEをセット）する
     * @param agent
     */
    public void deleteGroup(Agent agent) {
        isWolfGroupMap.forEach(((wolfGroup, boo) -> {
            if (wolfGroup.inAgent(agent)) {
                isWolfGroupMap.put(wolfGroup, Boolean.TRUE);
            }
        }));
    }
}
