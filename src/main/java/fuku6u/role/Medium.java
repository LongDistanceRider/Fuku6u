package fuku6u.role;

import fuku6u.Expectation.PossessedExpectation;
import fuku6u.Expectation.WolfGroupExpectation;
import fuku6u.board.BoardSurface;
import fuku6u.player.Utterance;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;

public class Medium extends AbstractRole {
    @Override
    public Role getRole() {
        return Role.MEDIUM;
    }

    @Override
    public void dayStart(GameInfo gameInfo, BoardSurface bs, WolfGroupExpectation wExpect, PossessedExpectation pExpect) {
        // とりあえずロケットCOしておく
        Utterance.getInstance().offer(Topic.COMINGOUT, bs.getMe(), Role.MEDIUM, "ボクは霊能者だよ。");  // CO
        Judge medium = gameInfo.getMediumResult();
        if (medium != null) {
            Agent target = medium.getTarget();
            Species result = medium.getResult();
            bs.putIdentifiedMap(target, result);  // 霊能結果を保管

            Utterance.getInstance().offer(Topic.IDENTIFIED, target, result, "霊能結果は" + result + "だったよ。");
        }
    }

    @Override
    public void talk(BoardSurface boardSurface) {

    }

    @Override
    public void finish(BoardSurface boardSurface) {

    }
}
