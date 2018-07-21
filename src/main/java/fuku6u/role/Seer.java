package fuku6u.role;

import fuku6u.Expectation.PossessedExpectation;
import fuku6u.Expectation.WolfGroupExpectation;
import fuku6u.board.BoardSurface;
import fuku6u.board.Util;
import fuku6u.player.Utterance;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Seer extends AbstractRole {

    /* GameSetting */
    private GameSetting gameSetting;
    /* 強制投票先 */
    private Agent forceVoteAgent = null;
    /* 占い結果 */
    private Map<Agent, Species> resultMap = new HashMap<>();

    public Seer(GameSetting gameSetting) {
        this.gameSetting = gameSetting;
    }

    @Override
    public Role getRole() {
        return Role.SEER;
    }

    @Override
    public void dayStart(GameInfo gameInfo, BoardSurface bs, WolfGroupExpectation wExpect, PossessedExpectation pExpect) {
        // TODO 5人人狼占い結果黒出し作戦を実装
        Utterance.getInstance().offer(Topic.COMINGOUT, bs.getMe(), Role.SEER, "ボクは占い師！");  // CO
        Judge divination = gameInfo.getDivineResult();
        if (divination != null) {
            Agent target = divination.getTarget();
            Species result = divination.getResult();
            resultMap.put(target, result);
            if (gameSetting.getPlayerNum() == 5) {  // 5人人狼占い結果黒出し作戦
                // targetが白の場合は，target以外に黒出し発言
                if (result.equals(Species.HUMAN)) {
                    // target以外に黒出し発言
                    List<Agent> candidateAgentList = gameInfo.getAliveAgentList();
                    candidateAgentList.remove(gameInfo.getAgent());
                    candidateAgentList.remove(target);
                    Agent lieBlackResultAgent = Util.randomElementSelect(candidateAgentList);
                    Utterance.getInstance().offer(Topic.DIVINED, lieBlackResultAgent, Species.WEREWOLF, lieBlackResultAgent + "を占った結果は人狼だよ！");
                    // 強制投票先に設定
                    forceVoteAgent = lieBlackResultAgent;
                }
            }
            Utterance.getInstance().offer(Topic.DIVINED, target, result, target + "を占った結果は" + Utterance.convertSpeciesToNl(result) + "だよ！");
        }
    }

    @Override
    public void talk(BoardSurface boardSurface) {

    }

    @Override
    public void finish(BoardSurface boardSurface) {
        forceVoteAgent = null;
    }
    @Override
    public List<Agent> vote(int day, BoardSurface boardSurface, List<Agent> candidateAgentList, WolfGroupExpectation wExpect, PossessedExpectation pExpect) {
        // 強制投票先が設定されている場合は，そのエージェントに投票
        if (forceVoteAgent != null) {
            return Arrays.asList(forceVoteAgent);
        }

        // 黒出ししている場合はそのエージェントに投票する
        for (Map.Entry<Agent, Species> resultMap :
                resultMap.entrySet()) {
            if (resultMap.getValue().equals(Species.WEREWOLF)) {
                if (candidateAgentList.contains(resultMap.getKey())) {   // 生存しているか
                    return Arrays.asList(resultMap.getKey());
                }
            }
        }

        // 人狼の可能性が高いエージェントに投票する
        List<Agent> maxDistrustAgentList = wExpect.getMaxDistrustAgent(candidateAgentList);
        if (!maxDistrustAgentList.isEmpty()) {
            return maxDistrustAgentList;
        }

        // 狂人の可能性が高いエージェントに投票する
        maxDistrustAgentList = pExpect.getMaxDistrustAgent(candidateAgentList);
        if (!maxDistrustAgentList.isEmpty()) {
            return maxDistrustAgentList;
        }

        // 適当なリストを返す
        return candidateAgentList;
    }
}
