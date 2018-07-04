package fuku6u.role;

import fuku6u.Expectation.WolfGroupExpectation;
import fuku6u.board.BoardSurface;
import fuku6u.board.Util;
import fuku6u.player.Utterance;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人狼は狂人と同じく占い師COする戦略
 */
public class Werewolf extends AbstractRole {

    /* 占い結果 */
    Map<Agent, Species> divinedMap = new HashMap<>();
    /* 人狼メンバー */
    List<Agent> werewolfList = new ArrayList<>();

    @Override
    public Role getRole() {
        return Role.WEREWOLF;
    }

    @Override
    public void dayStart(GameInfo gameInfo, BoardSurface bs, WolfGroupExpectation wExpect) {
        // TODO 戦略として占い師 COすることが状況を良くするのか，常に白だしするだけでいいのかを考慮する必要がある
        Utterance.getInstance().offer(Topic.COMINGOUT, bs.getMe(), Role.SEER);  // CO
        //  ----- 占い結果を作成する -----
        List<Agent> candidatesAgentList = gameInfo.getAliveAgentList();
        candidatesAgentList.remove(bs.getMe());
        // すでに占ったプレイヤは削除
        divinedMap.forEach(((agent, species) -> candidatesAgentList.remove(agent)));
        // 適当な相手を占ったことにする
        Agent target = Util.randomElementSelect(candidatesAgentList);
        // 占い結果を保管
        divinedMap.put(target, Species.HUMAN);
        // 占い結果を発言
        Utterance.getInstance().offer(Topic.DIVINED, target, Species.HUMAN);    // 「targetを占った結果白だった」
        String str = "1";
        for (int i = 0; i < Integer.parseInt(str); i++) {

        }
    }

    @Override
    public void talk(BoardSurface boardSurface) {

    }

    @Override
    public void finish(BoardSurface boardSurface) {

    }

    @Override
    public List<Agent> vote(List<Agent> candidateAgentList, WolfGroupExpectation wExpect) {
        // TODO 人狼用に書き換えること
        int maxDistrust = 0;
        List<Agent> mostDistrustAgentList = null;
        for (Agent agent :
                candidateAgentList) {
            int distrust = wExpect.getAgentDistrust(agent);
            if (distrust > maxDistrust) {
                mostDistrustAgentList.clear();
                maxDistrust = distrust;
            }
            if (distrust == maxDistrust) {
                mostDistrustAgentList.add(agent);
            }
        }
        return mostDistrustAgentList;
    }
}
