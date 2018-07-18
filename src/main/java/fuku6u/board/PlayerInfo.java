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
    /* 投票先発言リスト */
    private List<Agent> voteList = new ArrayList<>();

    public Agent getAgent() {
        return agent;
    }

    public List<Role> getSelfCO() {
        return selfCO;
    }

    public Map<Agent, Species> getDivMap() {
        return divMap;
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

    /**
     * 投票先発言を保管
     * @param target
     *  投票先エージェント
     */
    public void addVoteList(Agent target) {
        voteList.add(target);
    }

    public Map<Agent, Species> getIdenMap() {
        return idenMap;
    }
}
