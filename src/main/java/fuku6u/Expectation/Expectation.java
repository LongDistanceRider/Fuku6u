package fuku6u.Expectation;

import org.aiwolf.common.data.Agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Expectation {

    protected Map<Agent, Integer> agentDistrustMap = new HashMap<>();
    protected List<String> flagList = new ArrayList<>();
    protected List<Agent> clearAgentList = new ArrayList<>();
    protected List<Agent> convictionAgentList = new ArrayList<>();

    public List<Agent> getClearAgentList() {
        return clearAgentList;
    }

    /**
     * 容疑が晴れたエージェント（確定白など）を登録する
     * @param clearAgent
     */
    public abstract void clearAgent(Agent clearAgent);

    /**
     * 人狼または狂人だと確信したエージェント（確定黒など）を登録する
     * @param convictionAgent
     */
    public abstract void convictionAgent(Agent convictionAgent);

    /**
     * エージェントの疑い度を返す
     * @param agent
     * @return エージェントの疑い度　@paramに不正なエージェントが指定された場合，0が返却される
     */
    public abstract int getAgentDistrust(Agent agent);

    /**
     * エージェントの疑い度の高いエージェントを返す
     * @param agentList 疑い度を調べるエージェントのリスト
     * @return 最大の疑い度のあるエージェントのリストを返す
     */
    public List<Agent> getMaxDistrustAgent(List<Agent> agentList) {
        List<Agent> maxDistrustAgentList = new ArrayList<>();
        int maxDistrust = 0;
        for (Agent agent :
                agentList) {
            int distrust = getAgentDistrust(agent);
            if (distrust > maxDistrust) {
                maxDistrustAgentList.clear();
                maxDistrust = distrust;
            }
            if (distrust == maxDistrust) {
                maxDistrustAgentList.add(agent);
            }
        }
        return maxDistrustAgentList;
    }

    public void distrustCalc(Agent agent, Parameter addDistrust) {
        // 同じ箇所から同じ情報を複数加算することを回避するために，フラグ処理を施す
        StackTraceElement[] stackTraceElements = (new Throwable()).getStackTrace(); // スタックトレースより呼び出し元情報の取り出し
        String methodName = stackTraceElements[1].getMethodName();
        String className = stackTraceElements[1].getClassName();
        int line = stackTraceElements[1].getLineNumber();

        String flagString = className + methodName + line + agent + addDistrust; // フラグ名作成
        if (!flagList.contains(flagString)) {
            int preDistrust = agentDistrustMap.getOrDefault(agent, 0);
            int distrust = preDistrust + addDistrust.getInt();
            // agentの不信度を更新
            agentDistrustMap.put(agent, distrust);
            flagList.add(flagString);
        }
    }
}
