package fuku6u.role;

import fuku6u.board.BoardSurface;
import org.aiwolf.common.data.Role;

public class Villager extends AbstractRole {

    @Override
    public Role getRole() {
        return Role.VILLAGER;
    }

    @Override
    public void dayStart(BoardSurface boardSurface) {

    }

    @Override
    public void talk(BoardSurface boardSurface) {

    }

    @Override
    public void finish(BoardSurface boardSurface) {

    }
}
