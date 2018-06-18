package fuku6u.player;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class Dammy implements Player {

    @Override
    public String getName() {
        return "Dammy";
    }

    @Override
    public void update(GameInfo gameInfo) {

    }

    @Override
    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
    }

    @Override
    public void dayStart() {

    }

    @Override
    public String talk() {
        return "Over";
    }

    @Override
    public String whisper() {
        return null;
    }

    @Override
    public Agent vote() {
        return null;
    }

    @Override
    public Agent attack() {
        return null;
    }

    @Override
    public Agent divine() {
        return null;
    }

    @Override
    public Agent guard() {
        return null;
    }

    @Override
    public void finish() {

    }
}

