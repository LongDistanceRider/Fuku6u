package fuku6u.observer;

import fuku6u.board.BoardSurface;
import fuku6u.possessedExpectation.PossessedExpectation;
import fuku6u.wolfGroupExpectation.WolfGroupExpectation;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;

import java.util.List;
import java.util.Map;

public class TalkEndObserver extends Observer{
    GameInfo gameInfo;
    BoardSurface boardSurface;
    WolfGroupExpectation wExpect;
    PossessedExpectation pExpect;

    public TalkEndObserver(GameInfo gameInfo, BoardSurface boardSurface, WolfGroupExpectation wExpect, PossessedExpectation pExpect) {
        this.gameInfo = gameInfo;
        this.boardSurface = boardSurface;
        this.wExpect = wExpect;
        this.pExpect = pExpect;
    }
    public void check() {
        // 真占い師チェック
        if (!boardSurface.getAssignRole().getRole().equals(Role.SEER)) {
            List<Agent> seerCoAgent = boardSurface.getComingOutAgentList(Role.SEER);
            seerCoAgent.remove(lieSeerAgentList);
            if (seerCoAgent.size() == 1) {
                wExpect.deleteGroup(seerCoAgent.get(0));
                pExpect.clearAgent(seerCoAgent.get(0));
                Map<Agent, Species> divinedResult = boardSurface.getDivinedResult(seerCoAgent.get(0));
                divinedResult.forEach(((agent, species) -> {
                    if (species.equals(Species.HUMAN)) {
                        wExpect.deleteGroup(agent);
                        pExpect.clearAgent(agent);
                    } else {
                        wExpect.remainGroup(agent);
                        pExpect.clearAgent(agent);
                    }
                }));
            }
        }
        // 真霊能者チェック
        if (!boardSurface.getAssignRole().getRole().equals(Role.MEDIUM)) {
            List<Agent> mediumCoAgent = boardSurface.getComingOutAgentList(Role.MEDIUM);
            mediumCoAgent.remove(lieMediumAgentList);
            if (mediumCoAgent.size() == 1) {
                wExpect.deleteGroup(mediumCoAgent.get(0));
                pExpect.clearAgent(mediumCoAgent.get(0));
                Map<Agent, Species> mediumResult = boardSurface.getIdenResult(mediumCoAgent.get(0));
                mediumResult.forEach(((agent, species) -> {
                    if (species.equals(Species.HUMAN)) {
                        wExpect.deleteGroup(agent);
                        pExpect.clearAgent(agent);
                    } else {
                        wExpect.remainGroup(agent);
                        pExpect.clearAgent(agent);
                    }
                }));
            }
        }
    }
}
