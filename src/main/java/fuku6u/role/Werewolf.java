package fuku6u.role;

import fuku6u.Expectation.WolfGroupExpectation;
import fuku6u.board.BoardSurface;
import fuku6u.board.Util;
import fuku6u.log.Log;
import fuku6u.player.Utterance;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人狼は狂人と同じく占い師COする戦略
 */
public class Werewolf extends AbstractRole {

    /* 占い結果 */
    Map<Agent, Species> divinedMap = new HashMap<>();
    /* 人狼メンバー */
    List<Agent> werewolfList = new ArrayList<>();

    @Override
    public Role getRole() {
        return Role.WEREWOLF;
    }

    @Override
    public void dayStart(GameInfo gameInfo, BoardSurface bs, WolfGroupExpectation wExpect) {
        // TODO 戦略として占い師 COすることが状況を良くするのか，常に白だしするだけでいいのかを考慮する必要がある
        Utterance.getInstance().offer(Topic.COMINGOUT, bs.getMe(), Role.SEER, "ボクが本当の占い師だよ！対抗に騙されないで！");  // CO
        //  ----- 占い結果を作成する -----
        List<Agent> candidatesAgentList = gameInfo.getAliveAgentList();
        candidatesAgentList.remove(bs.getMe());
        // すでに占ったプレイヤは削除
        divinedMap.forEach(((agent, species) -> candidatesAgentList.remove(agent)));
        // 占い候補がいない場合（6日目ぐらいで既に占ったプレイヤしか生き残らなくなる）はスキップ
        if (!candidatesAgentList.isEmpty()) {
            // 適当な相手を占ったことにする
            Agent target = Util.randomElementSelect(candidatesAgentList);
            Log.trace("偽占い結果白出し: " + target);
            // 占い結果を保管
            divinedMap.put(target, Species.HUMAN);
            // 占い結果を発言
            Utterance.getInstance().offer(Topic.DIVINED, target, Species.HUMAN, target + "の占い結果は白だね。");    // 「targetを占った結果白だった」
        }
    }

    @Override
    public void talk(BoardSurface boardSurface) {

    }

    @Override
    public void finish(BoardSurface boardSurface) {

    }
    // TODO vote()をオーバーライドして人狼用に書き換えること
}
