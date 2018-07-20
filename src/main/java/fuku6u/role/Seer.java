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

public class Seer extends AbstractRole {
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
            bs.putDivinedMap(target, result);     // 占い結果を保管

            Utterance.getInstance().offer(Topic.DIVINED, target, result, target + "を占った結果は" + result + "だよ！");   // 「targetを占った結果resultだった」
        }
    }

    @Override
    public void talk(BoardSurface boardSurface) {

    }

    @Override
    public void finish(BoardSurface boardSurface) {

    }
}
