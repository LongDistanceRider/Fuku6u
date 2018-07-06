package fuku6u.observer;

import fuku6u.Expectation.Parameter;
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
            Agent genuineSeer = checkGenuineSeer(boardSurface, Role.SEER);
            if (genuineSeer != null) {
                wExpect.clearAgent(genuineSeer);
                pExpect.clearAgent(genuineSeer);
                Map<Agent, Species> divinedResult = boardSurface.getDivinedResult(genuineSeer);
                trustResult(divinedResult);
            }
        }
        // 真霊能者チェック
        if (!boardSurface.getAssignRole().getRole().equals(Role.MEDIUM)) {
            Agent genuineMedium = checkGenuineSeer(boardSurface, Role.MEDIUM);
            if (genuineMedium != null) {
                wExpect.clearAgent(genuineMedium);
                pExpect.clearAgent(genuineMedium);
                Map<Agent, Species> mediumResult = boardSurface.getIdenResult(genuineMedium);
                trustResult(mediumResult);
            }
        }
        // TODO 偽占い師偽霊能者の結果をリセットする必要がある
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

    private void trustResult(Map<Agent, Species> resultMap) {
        resultMap.forEach(((agent, species) -> {
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
