package fuku6u.player;

import com.sun.jmx.remote.internal.ArrayQueue;
import fuku6u.board.Util;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.random;

public class Dammy implements Player {

    GameInfo gameInfo;
    ArrayDeque<String> talkList = new ArrayDeque<>();

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
        talkList.add("Agent[01]が人狼じゃないのはわかってるんだよね");
        talkList.add("人狼COするね！");
        talkList.add("Agent[03]がいない！食べられちゃったのかな");
        talkList.add("人狼ってチャットでもできるよ");
    }


    @Override
    public void dayStart() {

    }

    @Override
    public String talk() {
        double randomInteger =  Math.random();
        int day = gameInfo.getDay();
        if (day == 1) {
            if (randomInteger < 0.5) {
                String talkString = talkList.poll();
                return talkString;
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

