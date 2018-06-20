package fuku6u.wolfGroupExpectation;

import org.aiwolf.common.data.Agent;

import java.util.ArrayList;
import java.util.List;

class WolfGroup {

    /* エージェントリスト */
    List<Agent> groupList = new ArrayList<>();

    public List<Agent> getGroupList() {
        return groupList;
    }

    /**
     * コンストラクタ
     * @param agentList
     *  グループに入れるエージェント
     */
    public WolfGroup(List<Agent> agentList) {
        this.groupList = agentList;
    }

    /**
     * エージェントがグループにいるか
     * @param agent
     * @return
     */
    public boolean inAgent(Agent agent) {
        for (Agent groupAgent :
                groupList) {
            if (groupAgent.equals(agent)) {
                return true;
            }
        }
        return false;
    }


}
