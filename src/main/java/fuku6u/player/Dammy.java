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
        talkList.add("私が占い師だからAgent[02]さんが裏切り者だと思うわ");
        talkList.add("今日の占い結果はAgent[03]さんが人間だったよ");
        talkList.add("今日はAgent[04]に投票しよう");
        talkList.add("楽しそう！");
        talkList.add("今日襲われるのは僕だよね。いやだなぁ");
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

