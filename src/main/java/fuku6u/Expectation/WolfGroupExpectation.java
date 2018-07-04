package fuku6u.Expectation;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WolfGroupExpectation extends Expectation {

    /* WolfGroupマップ */
    private Map<WolfGroup, Boolean> isWolfGroupMap = new HashMap<>();
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
        }
    }



    @Override
    public void clearAgent(Agent clearAgent) {
        clearAgentList.add(clearAgent);
        isWolfGroupMap.forEach(((wolfGroup, boo) -> {
            if (boo) {
                if (wolfGroup.inAgent(clearAgent)) {
                    isWolfGroupMap.put(wolfGroup, Boolean.FALSE);
                }
            }
        }));
    }

    @Override
    public void convictionAgent(Agent convictionAgent) {
        convictionAgentList.add(convictionAgent);
        isWolfGroupMap.forEach(((wolfGroup, boo) -> {
            if (boo) {
                if (!wolfGroup.inAgent(convictionAgent)) {
                    isWolfGroupMap.put(wolfGroup, Boolean.FALSE);
                }
            }
        }));
    }

    @Override
    public int getAgentDistrust(Agent agent) {
        if (clearAgentList.contains(agent)) return 0;
        if (convictionAgentList.contains(agent)) return Parameter.conviction.getInt();

        int distrustValue = agentDistrustMap.getOrDefault(agent, 0);

        for (Map.Entry<WolfGroup, Boolean> wolfGroupMap :
                isWolfGroupMap.entrySet()) {
            if (wolfGroupMap.getValue()) {
                for (Agent inGroupAgent :
                        wolfGroupMap.getKey().getGroupList()) {
                    if (inGroupAgent.equals(agent)) continue;
                    distrustValue += agentDistrustMap.getOrDefault(inGroupAgent, 0);
                }
            }
        }
        return distrustValue;
    }
}
