package fuku6u.board;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import java.util.*;

/**
 * package-private
 */
class PlayerInfo {
    /* Agent情報 */
    private Agent agent;
    /* comingoutした役職 */
    private List<Role> selfCO = new ArrayList<>();
    /* 占い結果リスト */
    private Map<Agent, Species> divMap = new HashMap<>();
    /* 霊能結果リスト */
    private Map<Agent, Species> idenMap = new HashMap<>();
    /* 襲撃されたか */
    private boolean isAttacked = false;
    /* 追放されたか */
    private boolean isExecuted = false;

    public Agent getAgent() {
        return agent;
    }

    public List<Role> getSelfCO() {
        return selfCO;
    }

    public Map<Agent, Species> getDivMap() {
        return divMap;
    }

    public boolean isAttacked() {
        return isAttacked;
    }

    public boolean isExecuted() {
        return isExecuted;
    }

    public void setAttacked(boolean attacked) {
        isAttacked = attacked;
    }

    public void setExecuted(boolean executed) {
        isExecuted = executed;
    }

    /**
     * コンストラクタ
     * @param agent
     */
    PlayerInfo(Agent agent) {
        this.agent = agent;
    }
    void addComingoutRole (Role role) {
        selfCO.add(role);
    }

    /**
     * 占い結果を保管
     * @param target
     * @param result
     */
    void putDivMap (Agent target, Species result) {
        divMap.put(target, result);
    }

    /**
     * 霊能結果を保管
     * @param target
     * @param result
     */
    public void putIdenMap(Agent target, Species result) {
        idenMap.put(target, result);
    }

    /**
     * ある役職をカミングアウトしているか
     * @param role
     */
    boolean isComingoutRole(Role role) {
        for (Role coRole :
                selfCO) {
            if (coRole.equals(role)) return true;
        }
        return false;
    }
}
