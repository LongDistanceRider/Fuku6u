package fuku6u.wolfGroupExpectation;

import org.aiwolf.common.data.Agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WolfGroupExpectation {

    /* WolfGroupマップ */
    private static Map<WolfGroup, Boolean> isWolfGroupMap = new HashMap<>();
    /* Agentの灰・黒可能性フラグ（各白はfalse） */
    private static Map<Agent, Boolean> isAgentDistrustMap = new HashMap<>();

    /**
     * コンストラクタ
     * @param agentList
     *  参加者エージェント（自分自身を除く）
     */
    public WolfGroupExpectation(List<Agent> agentList) {
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
        }
    }
}
