package fuku6u.role;

import fuku6u.board.BoardSurface;
import fuku6u.wolfGroupExpectation.WolfGroupExpectation;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

/**
 * 役職固有クラスの継承元クラス
 */
public abstract class AbstractRole {

    public abstract  Role getRole();

    public abstract void dayStart(GameInfo gameInfo, BoardSurface bs, WolfGroupExpectation wExpect);

    public abstract  void talk(BoardSurface boardSurface);

    public abstract void finish(BoardSurface boardSurface);
}
