package fuku6u.player;

import fuku6u.board.Util;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.random;

public class Dammy implements Player {

    GameInfo gameInfo;
    List<String> talkList = new ArrayList<>();

    @Override
    public String getName() {
        return "Dammy";
    }

    @Override
    public void update(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    @Override
    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
        talkList.add("私は村人です。");
        talkList.add("こんにちは");
        talkList.add("私が占い師なので、Agent[02]は偽物です。");
    }


    @Override
    public void dayStart() {

    }

    @Override
    public String talk() {
        double randomInteger =  Math.random();
        int day = gameInfo.getDay();
        if (day == 1) {
            if (randomInteger < 3) {
                return Util.randomElementSelect(talkList);
            }
        }
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

