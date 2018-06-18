package fuku6u.wolfGroupExpectation;

import org.aiwolf.common.data.Agent;

import java.util.ArrayList;
import java.util.List;

class WolfGroup {

    /* エージェントリスト */
    List<Agent> agentList = new ArrayList<>();

    public WolfGroup(List<Agent> agentList) {
        this.agentList = agentList;
    }
}
