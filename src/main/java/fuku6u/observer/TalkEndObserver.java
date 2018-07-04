package fuku6u.observer;

import fuku6u.Expectation.PossessedExpectation;
import fuku6u.Expectation.WolfGroupExpectation;
import fuku6u.board.BoardSurface;
import fuku6u.log.Log;
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
                wExpect.clearAgent(seerCoAgent.get(0));
                pExpect.clearAgent(seerCoAgent.get(0));
                Map<Agent, Species> divinedResult = boardSurface.getDivinedResult(seerCoAgent.get(0));
                divinedResult.forEach(((agent, species) -> {
                    if (species.equals(Species.HUMAN)) {
                        wExpect.clearAgent(agent);
                        pExpect.clearAgent(agent);
                    } else {
                        wExpect.convictionAgent(agent);
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
                wExpect.clearAgent(mediumCoAgent.get(0));
                pExpect.clearAgent(mediumCoAgent.get(0));
                Map<Agent, Species> mediumResult = boardSurface.getIdenResult(mediumCoAgent.get(0));
                mediumResult.forEach(((agent, species) -> {
                    if (species.equals(Species.HUMAN)) {
                        wExpect.clearAgent(agent);
                        pExpect.clearAgent(agent);
                    } else {
                        wExpect.convictionAgent(agent);
                        pExpect.clearAgent(agent);
                    }
                }));
            }
        }
        // TODO 黒を確信したエージェントに対して白出ししている占霊は偽物

        // TODO 確定白が存在する場合 => そのエージェントがいるグループは削除
        // TODO 確定黒が存在する場合 => そのエージェントがいるグループのみ残す
        // TODO ○-○進行によって処理を変える(後回し)
//        int seerCONum = bs.getComingOutAgentList(Role.SEER).size();
//        int mediumCONum = bs.getComingOutAgentList(Role.MEDIUM).size();
//        String progress = seerCONum + "-" + mediumCONum;
//        switch (progress) {
//            case "0-0":
//                break;
//            case "0-1":
//                break;
//            case "1-0":
//                break;
//            case "1-1":
//                break;
//            case "1-2":
//                break;
//            case "1-3":
//                break;
//            default:
//                Log.debug("想定していない進行を確認:" + progress);
//        }
        // TODO 占霊狩COしたプレイヤは1人か（自身が占霊狩の場合は除く）

    }
}
