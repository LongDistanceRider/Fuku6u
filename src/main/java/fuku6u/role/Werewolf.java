package fuku6u.role;

import fuku6u.Expectation.Parameter;
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

import java.util.*;

/**
 * 人狼は狂人と同じく占い師COする戦略
 */
public class Werewolf extends AbstractRole {

    /* 占い結果 */
    private Map<Agent, Species> divinedMap = new HashMap<>();
    /* 人狼メンバー */
    private List<Agent> werewolfList = new ArrayList<>();
    /* vote再投票フラグ */
    private boolean isReVote = false;
    /* 前回投票したエージェント（再投票時に利用） */
    private Agent preVoteAgent;

    @Override
    public Role getRole() {
        return Role.WEREWOLF;
    }

    @Override
    public void dayStart(GameInfo gameInfo, BoardSurface bs, WolfGroupExpectation wExpect, PossessedExpectation pExpect) {
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
        // パワープレイ判定（3人になった場合の判定．人狼COがあった場合のPP判定は別の場所で処理
        List<Agent> aliveAgentList = gameInfo.getAliveAgentList();
        if (gameInfo.getAliveAgentList().size() == 3) {
            // 狂人が存在するかを判定
            aliveAgentList.remove(gameInfo.getAgent());
            for (Agent agent :
                    aliveAgentList) {
                if (pExpect.getAgentDistrust(agent) >= Parameter.conviction.getInt()){
                    // PP発生
                    isPP = true;
                    Utterance.getInstance().offer(Topic.COMINGOUT, bs.getMe(), Role.WEREWOLF, "わおーん。実はボク、人狼なんだ");
                }
            }
        }
    }

    @Override
    public void talk(BoardSurface boardSurface) {

    }

    @Override
    public void finish(BoardSurface boardSurface) {
        isPP = false;
        preVoteAgent = null;

    }
    // TODO vote()をオーバーライドして人狼用に書き換えること

    @Override
    public List<Agent> vote(int day, BoardSurface boardSurface, List<Agent> candidateAgentList, WolfGroupExpectation wExpect, PossessedExpectation pExpect) {
        if (isPP) { // PP発生時
            // 狂人COしているAgentを取得
            List<Agent> possessedCoAgentList = boardSurface.getComingOutAgentList(Role.POSSESSED);
            // 人狼COしているAgentを取得
            List<Agent> werewolfCoAgentList = boardSurface.getComingOutAgentList(Role.WEREWOLF);
            // 狂狼COしているAgentリスト
            List<Agent> roleCoAgentList = new ArrayList<>();
            roleCoAgentList.addAll(possessedCoAgentList);
            roleCoAgentList.addAll(werewolfCoAgentList);
            roleCoAgentList.retainAll(candidateAgentList);
            // 人狼仲間を除く
            roleCoAgentList.removeAll(boardSurface.getWerewolfList());
            // 狂狼COしているAgentが1人か
            if (roleCoAgentList.size() == 1) {
                // 狂狼COしていないAgentに投票
                List<Agent> ppCandidateAgentList = candidateAgentList;
                // 人狼仲間は除く
                ppCandidateAgentList.removeAll(boardSurface.getWerewolfList());
                ppCandidateAgentList.removeAll(roleCoAgentList);
                if (preVoteAgent == null) { // 再投票時に別エージェントに投票するように処理をする
                    preVoteAgent = Util.randomElementSelect(ppCandidateAgentList);
                    return Arrays.asList(preVoteAgent);
                } else {
                    return roleCoAgentList;
                }
            } else if (roleCoAgentList.size() == 2) {
                if (preVoteAgent == null) {
                    // 狂人の可能性が低い方に投票する
                    int distrust1 = pExpect.getAgentDistrust(roleCoAgentList.get(0));
                    int distrust2 = pExpect.getAgentDistrust(roleCoAgentList.get(1));
                    if (distrust1 < distrust2) {
                        preVoteAgent = roleCoAgentList.get(0);
                    } else {
                        preVoteAgent = roleCoAgentList.get(1);
                    }
                    return Arrays.asList(preVoteAgent);
                } else {
                    roleCoAgentList.remove(preVoteAgent);
                    return roleCoAgentList;
                }
            }
        }

        // 1.人狼以外に黒出しされているエージェントに投票
        List<Agent> divinedBlackAgentList = boardSurface.getDivinedBlackAgentList();
        List<Agent> tmpCandidateAgentList = candidateAgentList;
        tmpCandidateAgentList.removeAll(boardSurface.getWerewolfList());
        tmpCandidateAgentList.retainAll(divinedBlackAgentList);    // 候補者 AND 黒出しエージェント　のリストに変換
        if (!tmpCandidateAgentList.isEmpty()) {
            return tmpCandidateAgentList;
        }

        // 2.役職COしていないエージェント　かつ　投票数の多いエージェント
        List<Agent> notCoRoleAndMaxVotedAgentList = new ArrayList<>();
        List<Agent> roleCoAgentList = boardSurface.getComingOutAgentList(Role.SEER);
        roleCoAgentList.addAll(boardSurface.getComingOutAgentList(Role.MEDIUM));
        roleCoAgentList.addAll(boardSurface.getComingOutAgentList(Role.BODYGUARD));
        List<Agent> maxVotedAgentList = boardSurface.getMaxVotedAgentList(day, candidateAgentList);
        maxVotedAgentList.forEach(agent -> {
            if (!roleCoAgentList.contains(agent)) {
                notCoRoleAndMaxVotedAgentList.add(agent);
            }
        });
        if (!notCoRoleAndMaxVotedAgentList.isEmpty()) {
            return notCoRoleAndMaxVotedAgentList;
        }

        // 3.投票数の多いエージェント
        if (!maxVotedAgentList.isEmpty()) {
            return maxVotedAgentList;
        }

        // 4.狂人っぽくないエージェント
        int minDistrust = 0;
        List<Agent> unlikePossessedAgentList = new ArrayList<>();
        for (Agent candidateAgent :
                candidateAgentList) {
            int distrust = pExpect.getAgentDistrust(candidateAgent);
            if (distrust < minDistrust) {
                minDistrust = distrust;
                unlikePossessedAgentList.clear();
            }
            if (distrust == minDistrust) {
                unlikePossessedAgentList.add(candidateAgent);
            }
        }
        if (!unlikePossessedAgentList.isEmpty()) {
            return unlikePossessedAgentList;
        }

        // 5.適当なエージェント
        return candidateAgentList;
    }
}
