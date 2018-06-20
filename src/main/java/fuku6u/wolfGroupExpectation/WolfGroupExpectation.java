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
    /* WolfGroupの不信度 */
    private Map<WolfGroup, Integer> wolfGroupDistrustMap = new HashMap<>();
    /* Agentの灰・黒可能性フラグ（各白はfalse） */ // TODO このリスト必要ないかも　要検討
    private Map<Agent, Boolean> isAgentDistrustMap = new HashMap<>();
    /* エージェントの不信度 */
    private Map<Agent, Integer> agentDistrustMap = new HashMap<>();
    /* 重複計算の回避リスト */
    List<String> flagList = new ArrayList<>();

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
                    wolfGroupDistrustMap.put(wolfGroup, 0);
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
        // 同じ箇所から同じ情報を複数加算することを回避するために，フラグ処理を施す
        StackTraceElement[] stackTraceElements = (new Throwable()).getStackTrace(); // スタックトレースより呼び出し元情報の取り出し
        String methodName = stackTraceElements[1].getMethodName();
        String className = stackTraceElements[1].getClassName();
        int line = stackTraceElements[1].getLineNumber();

        String flagString = className + methodName + line + agent + addDistrust; // フラグ名作成
        if (!flagList.contains(flagString)) {
            int preDistrust = agentDistrustMap.get(agent);
            int distrust = preDistrust + addDistrust;
            agentDistrustMap.put(agent, distrust);
            flagList.add(flagString);
        }
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

    /**
     * 評価値の高いグループの中から投票すべきエージェントを返す
     * 同数の場合は複数返す
     * @return
     * 　評価値が高いグループの中にいるエージェントの中から，重複数を考慮した不信度の高いエージェントを返す
     */
    public List<Agent> getBlackAgent() {
        List<WolfGroup> blackList = getBlackList();
        List<Agent> inGroupAgentList = new ArrayList<>();
        // ブラックリストグループ内で存在するエージェントのうち，疑い度が高いエージェントを返す
        for (WolfGroup wolfGroup :
                blackList) {
            inGroupAgentList.addAll(wolfGroup.getGroupList());  // グループにいるエージェントを全て入れる（重複含む）
        }

        // 不信度の計算
        List<Agent> blackAgentList = new ArrayList<>();
        int max_value = 0;  // 最大不信度
        for (Agent blackAgent :
                inGroupAgentList) {
            Map<Agent, Integer> duplicateAgentCount = new HashMap<>();
            duplicateAgentCount.merge(blackAgent, 1, Integer::sum); // 重複数を数える
            // blackListにいるエージェントの不信度 + 重複数を計算
            int value = wolfGroupDistrustMap.get(blackAgent) + duplicateAgentCount.get(blackAgent);
            if (max_value < value) {
                max_value = value;
                blackAgentList.clear();
            }
            if (max_value == value) {
                blackAgentList.add(blackAgent);
            }
        }
        return blackAgentList;
    }
    /**
     * 評価値が高いグループを返す（同数の場合は複数返される）
     * @return
     */
    private List<WolfGroup> getBlackList() {
        List<WolfGroup> blackList = new ArrayList<>();
        int maxValue = 0;
        for (Map.Entry<WolfGroup, Boolean> wolfGroupBooleanEntry:
                isWolfGroupMap.entrySet()) {
            if (wolfGroupBooleanEntry.getValue().equals(Boolean.TRUE)) {    // グループが存在している
                int value = groupValue(wolfGroupBooleanEntry.getKey()); // グループの評価値を受け取る
                if (maxValue < value) {
                    maxValue = value;
                    blackList.clear();  // ブラックリストをクリア
                }
                if (maxValue == value) {
                    blackList.add(wolfGroupBooleanEntry.getKey());  // ブラックリストへ追加
                }
            }
        }
        return blackList;
    }

    /**
     * グループの評価値を返す
     * @param wolfGroup
     *  評価したいグループ
     * @return
     *  グループにいるエージェントの不信度の和とグループの不信度の和を返す
     */
    private int groupValue(WolfGroup wolfGroup) {
        List<Agent> groupAgentList = wolfGroup.getGroupList();
        int value = 0;
        for (Agent groupAgent :
                groupAgentList) {
            value += agentDistrustMap.get(groupAgent);  // グループにいるエージェントの不信度を足す
        }
        value += wolfGroupDistrustMap.get(wolfGroup);
        return value;
    }
}
