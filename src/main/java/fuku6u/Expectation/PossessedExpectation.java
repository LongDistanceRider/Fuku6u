package fuku6u.Expectation;

import org.aiwolf.common.data.Agent;

public class PossessedExpectation extends Expectation {
    @Override
    public void clearAgent(Agent clearAgent) {
        clearAgentList.add(clearAgent);
    }

    @Override
    public void convictionAgent(Agent convictionAgent) {
        convictionAgentList.add(convictionAgent);
    }

    @Override
    public int getAgentDistrust(Agent agent) {
        if (clearAgentList.contains(agent)) return 0;
        if (convictionAgentList.contains(agent)) return Parameter.conviction.getInt();
        return agentDistrustMap.getOrDefault(agent, 0);
    }
}
