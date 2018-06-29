package fuku6u.role;

import fuku6u.board.BoardSurface;
import fuku6u.player.Utterance;
import fuku6u.wolfGroupExpectation.WolfGroupExpectation;
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
    public void dayStart(GameInfo gameInfo, BoardSurface bs, WolfGroupExpectation wExpect) {
        Utterance.getInstance().offer(Topic.COMINGOUT, bs.getMe(), Role.SEER);  // CO
        Judge divination = gameInfo.getDivineResult();
        if (divination != null) {
            Agent target = divination.getTarget();
            Species result = divination.getResult();
            bs.putDivinedResultMap(target, result);     // 占い結果を保管

            Utterance.getInstance().offer(Topic.DIVINED, target, result);   // 「targetを占った結果resultだった」

            // 判定によって人狼グループ予想クラスの処理をする
            if (result.equals(Species.HUMAN)) {
                wExpect.deleteGroup(target);
            } else {
                wExpect.remainGroup(target);
            }
        }
    }

    @Override
    public void talk(BoardSurface boardSurface) {

    }

    @Override
    public void finish(BoardSurface boardSurface) {

    }
}
