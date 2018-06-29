package fuku6u.role;

import fuku6u.board.BoardSurface;
import fuku6u.wolfGroupExpectation.WolfGroupExpectation;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

public class Villager extends AbstractRole {

    @Override
    public Role getRole() {
        return Role.VILLAGER;
    }

    @Override
    public void dayStart(GameInfo gameInfo, BoardSurface bs, WolfGroupExpectation wExpect) {

    }

    @Override
    public void talk(BoardSurface boardSurface) {

    }

    @Override
    public void finish(BoardSurface boardSurface) {

    }
}
