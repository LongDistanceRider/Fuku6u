package fuku6u.board;

import fuku6u.log.Log;
import fuku6u.role.*;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardSurface {

    /* 自分のエージェント */
    private Agent me;
    /* 自分自身の役職 */
    private AbstractRole assignRole = null;
    /* 占い結果 */
    private Map<Agent, Species> divinedResultMap = new HashMap<>();
    /* 霊能結果 */
    private Map<Agent, Species> mediumResultMap = new HashMap<>();
    /* PlayerInfoリスト（自分自身は除く） */
    private List<PlayerInfo> playerInfoList = new ArrayList<>();
    /* 人狼メンバーリスト */
    private List<Agent> werewolfList = new ArrayList<>();

    // Getter
    public AbstractRole getAssignRole() {
        return assignRole;
    }
    public Agent getMe() {
        return me;
    }
    public Map<Agent, Species> getDivinedResultMap() {
        return divinedResultMap;
    }
    public List<Agent> getWerewolfList() {
        return werewolfList;
    }
    public Map<Agent, Species> getMediumResultMap() {
        return mediumResultMap;
    }

    // Setter
    public void putDivinedResultMap(Agent target, Species result) {
        divinedResultMap.put(target, result);
    }
    public void putMediumResultMap(Agent target, Species result) {
        mediumResultMap.put(target, result);
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
    public void setAssignRole(GameInfo gameInfo) {
        Role role = gameInfo.getRole();
        Log.info("MyRole: " + role);
        switch (role) {
            case SEER:
                assignRole = new Seer();
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
    public List<Agent> getCandidateDivinedAgentList () {
        List<Agent> candidateAgentList = new ArrayList<>();
        List<Agent> yetDivinedAgentList = getYetDivinedAgentList();
        List<Agent> aliveAgentList = getAliveAgentList();
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

    public Map<Agent, Species> getDivinedResult (Agent submit) {
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

    public Map<Agent,Species> getIdenResult(Agent submit) {
        PlayerInfo playerInfo = getPlayerInfo(submit);
        if (playerInfo != null) {
            return playerInfo.getIdenMap();
        } else {
            Log.warn("getIdenResultで渡された引数は不正です．submit: " + submit);
        }
        return null;
    }

    /**
     * 黒判定を出されたエージェントのリストを返す
     *
     * @return
     */
    public List<Agent> getBlackDivinedAgentList () {
        List<Agent> blackDivinedAgentList = new ArrayList<>();

        List<Agent> comingoutSeerList = getComingOutAgentList(Role.SEER);
        for (Agent seerAgent :
                comingoutSeerList) {
            Map<Agent, Species> divinedResultMap = getDivinedResult(seerAgent);
            for (Map.Entry<Agent, Species> divinedResult :
                    divinedResultMap.entrySet()) {
                if (divinedResult.getValue().equals(Species.WEREWOLF)) {
                    blackDivinedAgentList.add(divinedResult.getKey());
                }
            }
        }
        return blackDivinedAgentList;
    }

    /**
     * 白判定を出されたエージェントのリストを返す
     * @return
     */
    public List<Agent> getWhiteDivinedAgentList () {
        List<Agent> whiteDivinedAgentList = new ArrayList<>();

        List<Agent> comingoutSeerList = getComingOutAgentList(Role.SEER);
        for (Agent seerAgent :
                comingoutSeerList) {
            Map<Agent, Species> divinedResultMap = getDivinedResult(seerAgent);
            for (Map.Entry<Agent, Species> divinedResult :
                    divinedResultMap.entrySet()) {
                if (divinedResult.getValue().equals(Species.HUMAN)) {
                    whiteDivinedAgentList.add(divinedResult.getKey());
                }
            }
        }
        return whiteDivinedAgentList;
    }

    /*
        PlayerInfo操作
     */
    /**
     * カミングアウトした役職を保管
     * @param agent
     * @param role
     */
    public void addComingoutRole (Agent agent, Role role) {
        getPlayerInfo(agent).addComingoutRole(role);
    }

    /**
     * 占い結果を保管
     * @param agent
     * @param target
     * @param result
     */
    public void addDivMap (Agent agent, Agent target, Species result) {
        PlayerInfo playerInfo = getPlayerInfo(agent);
        playerInfo.putDivMap(target, result);
        // 占いCOしていることを確認　していなければ，占いCOとする
        if (!playerInfo.isComingoutRole(Role.SEER)) {
            addComingoutRole(agent, Role.SEER);
        }
    }

    /**
     * 霊能結果を保管
     * @param agent
     * @param target
     * @param result
     */
    public void addIdenMap (Agent agent, Agent target, Species result) {
        PlayerInfo playerInfo = getPlayerInfo(agent);
        playerInfo.putIdenMap(target, result);
        // 占いCOしていることを確認　していなければ，霊能COとする
        if (!playerInfo.isComingoutRole(Role.MEDIUM)) {
            addComingoutRole(agent, Role.MEDIUM);
        }
    }

    /**
     * 投票先発言を保管
     * @param submit
     * @param target
     */
    public void addVote(Agent submit, Agent target) {
        PlayerInfo playerInfo = getPlayerInfo(submit);
        playerInfo.addVoteList(target);
    }

    /**
     * 追放されたエージェントを保管
     * @param executedAgent
     */
    public void executedAgent(Agent executedAgent) {
        getPlayerInfo(executedAgent).setExecuted(true);
    }

    /**
     * 襲撃されたエージェントを保管
     * @param attackedAgent
     */
    public void attackedAgent(Agent attackedAgent) {
        getPlayerInfo(attackedAgent).setAttacked(true);
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
     * 生存しているプレイヤーを返す
     *
     * @return
     */
    private List<Agent> getAliveAgentList() {
        List<Agent> aliveAgentList = new ArrayList<>();
        for (PlayerInfo playerInfo :
                playerInfoList) {
            if (!playerInfo.isAttacked() && !playerInfo.isExecuted()) {
                aliveAgentList.add(playerInfo.getAgent());
            }
        }
        return aliveAgentList;
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
            if (!divinedResultMap.containsKey(participantAgent)) {  // まだ占われていないならリスト追加
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
}


