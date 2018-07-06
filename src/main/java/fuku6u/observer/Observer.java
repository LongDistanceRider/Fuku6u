package fuku6u.observer;

import fuku6u.board.BoardSurface;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 盤面状態観測クラス
 *
 */
public abstract class Observer {

    /* 偽COをしているエージェントを保管 */
    protected static Map<Role, List<Agent>> lieRoleAgentMapList = new HashMap<>();

    protected static void addlieRoleAgentMapList (Role role, Agent agent) {
        List<Agent> preAgentList = lieRoleAgentMapList.getOrDefault(role, new ArrayList<>());
        preAgentList.add(agent);
        lieRoleAgentMapList.put(role, preAgentList);
    }

    /**
     * 真占・霊・狩が確定しているかをチェックする
     *
     * @param boardSurface
     * @param role
     *
     * @return 真確定している場合はそのエージェントを返す．確定していない場合はnullを返す．
     */
    public static Agent checkGenuineSeer(BoardSurface boardSurface, Role role) {
        List<Agent> coAgent = boardSurface.getComingOutAgentList(role);
        coAgent.remove(lieRoleAgentMapList.get(role));

        if (coAgent.size() == 1) {
            return coAgent.get(0);
        }
        return null;
    }
}
