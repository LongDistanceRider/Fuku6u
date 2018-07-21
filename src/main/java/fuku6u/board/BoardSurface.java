package fuku6u.board;

import fuku6u.log.Log;
import fuku6u.role.*;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardSurface {

    /* 自分のエージェント */
    private Agent me;
    /* 自分自身の役職 */
    private AbstractRole assignRole = null;
    /* 占い結果 */ // TODO SEERクラスへ移動
    private Map<Agent, Species> divinedMap = new HashMap<>();
    /* 霊能結果 */ // TODO MEDIUMクラスへ移動
    private Map<Agent, Species> identifiedMap = new HashMap<>();
    /* PlayerInfoリスト（自分自身は除く） */
    private List<PlayerInfo> playerInfoList = new ArrayList<>();
    /* 人狼メンバーリスト */
    // TODO Werewolfクラスへ移動
    private List<Agent> werewolfList = new ArrayList<>();
    /* 追放されたエージェントリスト */
    private List<Agent> executedAgentList = new ArrayList<>();
    /* 襲撃されたエージェントリスト */
    private List<Agent> attackedAgentList = new ArrayList<>();

    // Getter
    public AbstractRole getAssignRole() {
        return assignRole;
    }
    public Agent getMe() {
        return me;
    }
    public Map<Agent, Species> getDivinedMap() {
        return divinedMap;
    }
    public List<Agent> getWerewolfList() {
        return werewolfList;
    }
    public Map<Agent, Species> getIdentifiedMap() {
        return identifiedMap;
    }

    // Setter
    public void putDivinedMap(Agent target, Species result) {
        divinedMap.put(target, result);
    }
    public void putIdentifiedMap(Agent target, Species result) {
        identifiedMap.put(target, result);
    }
    public void addExecutedAgentList(Agent executedAgent) {
        executedAgentList.add(executedAgent);
    }
    public void addAttackedAgentList(Agent attackedAgent) {
        attackedAgentList.add(attackedAgent);
    }

    /**
     * カミングアウトした役職を保管
     * @param agent カミングアウトしたエージェント
     * @param role カミングアウトした役職
     */
    public void addCoRole(Agent agent, Role role) {
        PlayerInfo playerInfo = getPlayerInfo(agent);
        if (playerInfo != null) {
            playerInfo.addComingoutRole(role);
        } else {
            Log.warn("addCoRoleで渡された引数は不正です．agent: " + agent);
        }
    }

    /**
     * コンストラクタ
     * @param gameInfo gameInfo
     */
    public BoardSurface(GameInfo gameInfo) {
        this.me = gameInfo.getAgent();
        for (Agent agent :
                gameInfo.getAgentList()) {
            if (agent == this.me) continue; // 自分自身はスキップ
            playerInfoList.add(new PlayerInfo(agent));
        }
    }

    /**
     * 役職をセット
     * @param gameInfo gameInfo
     */
    public void setAssignRole(GameInfo gameInfo, GameSetting gameSetting) {
        Role role = gameInfo.getRole();
        Log.info("MyRole: " + role);
        switch (role) {
            case SEER:
                assignRole = new Seer(gameSetting);
                break;
            case MEDIUM:
                assignRole = new Medium();
                break;
            case BODYGUARD:
                assignRole = new Bodyguard();
                break;
            case POSSESSED:
                assignRole = new Possessed();
                break;
            case WEREWOLF:
                assignRole = new Werewolf();
                // 人狼メンバーを追加
                Map<Agent, Role> roleMap = gameInfo.getRoleMap();
                roleMap.forEach(((agent, assignRole) -> {
                    if (!agent.equals(gameInfo.getAgent()) && assignRole.equals(Role.WEREWOLF)) {
                        werewolfList.add(agent);
                    }
                }));
                break;
            default:
                assignRole = new Villager();
        }
    }
    /**
     * 占い候補となるエージェントリストを返す
     *
     * このリストは　まだ占われていない　かつ　生存しているプレイヤを返す
     * @return 占える対象かつ，占っていないエージェントのリスト
     */
    public List<Agent> getCandidateDivinedAgentList (List<Agent> aliveAgentList) {
        List<Agent> candidateAgentList = new ArrayList<>();
        List<Agent> yetDivinedAgentList = getYetDivinedAgentList();
        yetDivinedAgentList.forEach(agent -> {
            if (aliveAgentList.contains(agent)) {
                candidateAgentList.add(agent);
            }
        });
        return candidateAgentList;
    }

    /**
     * ある役職をカミングアウトしたエージェントのリストを返す
     *
     * @param role 手に入れたい役職
     * @return roleで指定した役職をカミングアウトしたエージェントのリスト
     */
    public List<Agent> getComingOutAgentList (Role role) {
        List<Agent> comingoutAgentList = new ArrayList<>();
        for (PlayerInfo playerInfo :    // プレイヤ全探索
                playerInfoList) {
            List<Role> comingoutRoleList = playerInfo.getSelfCO();  // COした役職を取得
            for (Role comingoutRole :
                    comingoutRoleList) {
                if (comingoutRole.equals(role)) {   // 引数1 role をしたエージェントをリストに追加
                    comingoutAgentList.add(playerInfo.getAgent());
                }
            }
        }
        return comingoutAgentList;
    }

    /**
     * あるエージェントが発言した占い結果を返す
     *
     * @param submit 発言者エージェント
     * @return submitで指定したエージェントが発言した占い結果
     */

    public Map<Agent, Species> getDivinedMap (Agent submit) {
        PlayerInfo playerInfo = getPlayerInfo(submit);
        if (playerInfo != null) {
            return playerInfo.getDivMap();
        } else {
            Log.warn("getDivinedResultで渡された引数は不正です．submit: " + submit);
        }
        return null;
    }

    /**
     * あるエージェントが発言した霊能結果を返す
     * @param submit 発言者エージェント
     * @return submitで指定したエージェントが発言した霊能結果
     */

    public Map<Agent,Species> getIdentifiedMap(Agent submit) {
        PlayerInfo playerInfo = getPlayerInfo(submit);
        if (playerInfo != null) {
            return playerInfo.getIdenMap();
        } else {
            Log.warn("getIdenResultで渡された引数は不正です．submit: " + submit);
        }
        return null;
    }

    /**
     * 占い結果を保管
     * @param agent　占い結果を出したエージェント
     * @param target 占い先エージェント
     * @param result 占い結果
     */
    public void putDivinedMap(Agent agent, Agent target, Species result) {
        PlayerInfo playerInfo = getPlayerInfo(agent);
        playerInfo.putDivMap(target, result);
        // 占いCOしていることを確認　していなければ，占いCOとする
        if (!playerInfo.isComingoutRole(Role.SEER)) {
            addCoRole(agent, Role.SEER);
        }
    }

    /**
     * 霊能結果を保管
     * @param agent 霊能結果を出したエージェント
     * @param target 霊能先エージェント
     * @param result 霊能結果
     */
    public void putIdentifiedMap (Agent agent, Agent target, Species result) {
        PlayerInfo playerInfo = getPlayerInfo(agent);
        playerInfo.putIdenMap(target, result);
        // 占いCOしていることを確認　していなければ，霊能COとする
        if (!playerInfo.isComingoutRole(Role.MEDIUM)) {
            addCoRole(agent, Role.MEDIUM);
        }
    }

    /**
     * 投票先発言を保管
     * @param submit 投票先発言をしたエージェント
     * @param target 投票先エージェント
     */
    public void addVote(int day, Agent submit, Agent target) {
        PlayerInfo playerInfo = getPlayerInfo(submit);
        if (playerInfo != null) {
            playerInfo.addVoteList(day, target);
        } else {
            Log.warn("addVoteに不正な引数が渡されました．submit: " + submit);
        }
    }


    /*
        private
     */

    /**
     * プレイヤー情報リストからプレイヤー情報のインスタンスを返す
     * @param agent 欲しいプレイヤー情報
     * @return プレイヤー情報
     */
    private PlayerInfo getPlayerInfo(Agent agent) {
        for (PlayerInfo playerInformation :
                playerInfoList) {
            if (playerInformation.getAgent().equals(agent)) return playerInformation;
        }
        return null;
    }

    /**
     * 参加者エージェントを返す
     *  （自分は含まない）
     * @return
     */
    private List<Agent> getParticipantAgentList () {
        List<Agent> participantAgentList = new ArrayList<>();
        for (PlayerInfo playerInfo :
                playerInfoList) {
            participantAgentList.add(playerInfo.getAgent());
        }
        return participantAgentList;
    }

    /**
     * まだ占われていないエージェントのリストを返す
     * 　（襲撃されたエージェント，追放されたエージェントを含む）
     * @return
     */
    private List<Agent> getYetDivinedAgentList () {
        List<Agent> yetDivinedAgentList = new ArrayList<>();

        List<Agent> participantAgentList = getParticipantAgentList();   // 参加者エージェントリスト
        for (Agent participantAgent :
                participantAgentList) {
            if (!divinedMap.containsKey(participantAgent)) {  // まだ占われていないならリスト追加
                yetDivinedAgentList.add(participantAgent);
            }
        }
        return yetDivinedAgentList;
    }

    /**
     * agentがCOしている役職を返す
     * スライドしている場合は，最後のCO役職を返す
     * @param agent
     * @return
     */
    public Role getCoRole(Agent agent) {
        List<Role> selfCoRole = getPlayerInfo(agent).getSelfCO();
        if (selfCoRole.isEmpty()) {
            return null;
        }
        return selfCoRole.get(selfCoRole.size() - 1);
    }

    /**
     * 黒出しされているエージェントのリストを返す
     * @return 黒出しされているエージェントのリスト
     */
    public List<Agent> getDivinedBlackAgentList() {
        List<Agent> divinedBlackAgentList = new ArrayList<>();
        List<Agent> seerCoAgentList = getComingOutAgentList(Role.SEER);
        for (Agent seerCoAgent :
                seerCoAgentList) {
            Map<Agent, Species> divinedMap = getPlayerInfo(seerCoAgent).getDivMap();
            for (Map.Entry<Agent, Species> divinedEntry :
                    divinedMap.entrySet()) {
                if (divinedEntry.getValue().equals(Species.WEREWOLF)) {
                    divinedBlackAgentList.add(divinedEntry.getKey());
                }
            }
        }
        return divinedBlackAgentList;
    }

    /**
     * 最大投票数を得たエージェントリストを返す
     * @param day 指定された日の投票数をカウントする
     * @param candidateAgentList この候補者リスト内から最大投票数を得たエージェントリストを返す
     * @return 最大投票数を得たエージェントリスト
     */
    public List<Agent> getMaxVotedAgentList(int day, List<Agent> candidateAgentList) {
        Map<Agent, Integer> voteCountMap = new HashMap<>();
        for (PlayerInfo playerInfo :
                playerInfoList) {
            List<Agent> voteList = playerInfo.getVoteList(day);
            if (voteList != null) {
                for (Agent agent :
                        voteList) {
                    if (candidateAgentList.contains(agent)) {
                        int count = voteCountMap.getOrDefault(agent, 0);
                        count++;
                        voteCountMap.put(agent, count);
                    }
                }
            }
        }
        int maxCount = 0;
        List<Agent> maxCountAgent = new ArrayList<>();
        for (Map.Entry<Agent, Integer> voteCountEntry :
                voteCountMap.entrySet()) {
            int count = voteCountEntry.getValue();
            if (count > maxCount) {
                maxCount = count;
                maxCountAgent.clear();
            }
            if (count == maxCount) {
                maxCountAgent.add(voteCountEntry.getKey());
            }
        }
        return maxCountAgent;
    }

//    /**
//     * 黒判定を出されたエージェントのリストを返す
//     *
//     * @return
//     */
//    public List<Agent> getBlackDivinedAgentList () {
//        List<Agent> blackDivinedAgentList = new ArrayList<>();
//
//        List<Agent> comingoutSeerList = getComingOutAgentList(Role.SEER);
//        for (Agent seerAgent :
//                comingoutSeerList) {
//            Map<Agent, Species> divinedResultMap = getDivinedResult(seerAgent);
//            for (Map.Entry<Agent, Species> divinedResult :
//                    divinedResultMap.entrySet()) {
//                if (divinedResult.getValue().equals(Species.WEREWOLF)) {
//                    blackDivinedAgentList.add(divinedResult.getKey());
//                }
//            }
//        }
//        return blackDivinedAgentList;
//    }
//
//    /**
//     * 白判定を出されたエージェントのリストを返す
//     * @return
//     */
//    public List<Agent> getWhiteDivinedAgentList () {
//        List<Agent> whiteDivinedAgentList = new ArrayList<>();
//
//        List<Agent> comingoutSeerList = getComingOutAgentList(Role.SEER);
//        for (Agent seerAgent :
//                comingoutSeerList) {
//            Map<Agent, Species> divinedResultMap = getDivinedResult(seerAgent);
//            for (Map.Entry<Agent, Species> divinedResult :
//                    divinedResultMap.entrySet()) {
//                if (divinedResult.getValue().equals(Species.HUMAN)) {
//                    whiteDivinedAgentList.add(divinedResult.getKey());
//                }
//            }
//        }
//        return whiteDivinedAgentList;
//    }
}


