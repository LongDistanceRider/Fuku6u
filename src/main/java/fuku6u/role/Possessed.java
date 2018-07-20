package fuku6u.role;

import fuku6u.Expectation.PossessedExpectation;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Possessed extends AbstractRole {

    /* 占い結果 */
    Map<Agent, Species> divinedMap = new HashMap<>();

    @Override
    public Role getRole() {
        return Role.POSSESSED;
    }

    @Override
    public void dayStart(GameInfo gameInfo, BoardSurface bs, WolfGroupExpectation wExpect, PossessedExpectation pExpect) {
        // TODO 戦略として占い師 COすることが状況を良くするのか，常に白だしするだけでいいのかを考慮する必要がある
        Utterance.getInstance().offer(Topic.COMINGOUT, bs.getMe(), Role.SEER, "ボクは占い師です！");  // CO
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
            Utterance.getInstance().offer(Topic.DIVINED, target, Species.HUMAN, target + "を占った結果は人間だったよ！");    // 「targetを占った結果白だった」
        }

        // パワープレイ判定（3人になった場合の判定．人狼COがあった場合のPP判定は別の場所で処理
        if (gameInfo.getAliveAgentList().size() == 3) {
            // PP発生
            Utterance.getInstance().offer(Topic.COMINGOUT, bs.getMe(), Role.POSSESSED, "ご主人様！ボクが狂人だよ！");
        }

    }

    @Override
    public void talk(BoardSurface boardSurface) {

    }

    @Override
    public void finish(BoardSurface boardSurface) {

    }
    //TODO vote()をオーバーライドして狂人用に書き換えること
}
