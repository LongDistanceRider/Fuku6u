package fuku6u.role;

import fuku6u.board.BoardSurface;
import org.aiwolf.common.data.Role;

/**
 * 役職固有クラスの継承元クラス
 */
public abstract class AbstractRole {

    public abstract  Role getRole();

    public abstract void dayStart(BoardSurface boardSurface);

    public abstract  void talk(BoardSurface boardSurface);

    public abstract void finish(BoardSurface boardSurface);
}
