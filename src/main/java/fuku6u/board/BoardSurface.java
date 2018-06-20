package fuku6u.board;

import fuku6u.log.Log;
import fuku6u.role.AbstractRole;
import fuku6u.role.Villager;
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
    Map<Agent, Species> divinedResultMap = new HashMap<>();
    /* PlayerInfoリスト（自分自身は除く） */
    private List<PlayerInfo> playerInfoList = new ArrayList<>();

    public void setAssignRole(Role role) {
        Log.info("MyRole: " + role);
        // TODO しばらく役職は村人固定　随時追加
        assignRole = new Villager();
//        switch (role) {
//            case SEER:
//                assignRole = new Seer(gameInfo);
//                break;
//            case MEDIUM:
//                assignRole = new Medium(gameInfo);
//                break;
//            case BODYGUARD:
//                assignRole = new Bodyguard(gameInfo);
//                break;
//            case POSSESSED:
//                assignRole = new Possessed(gameInfo);
//                break;
//            case WEREWOLF:
//                assignRole = new Werewolf(gameInfo);
//                break;
//            default:
//                assignRole = new Villager(gameInfo);
//        }
    }

    public AbstractRole getAssignRole() {
        return assignRole;
    }

    public Agent getMe() {
        return me;
    }

    public BoardSurface(GameInfo gameInfo) {
        this.me = gameInfo.getAgent();
        for (Agent agent :
                gameInfo.getAgentList()) {
            if (agent == this.me) continue; // 自分自身はスキップ
            playerInfoList.add(new PlayerInfo(agent));
        }
    }
    
    /*
        情報加工
     */

    /**
     * 占い候補となるエージェントリストを返す
     *
     * このリストは　まだ占われていない　かつ　生存しているプレイヤを返す
     * @return
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
     * @param role
     * @return
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
     * @param submit
     * @return
     */
    public Map<Agent, Species> getDivinedResult (Agent submit) {
        return getPlayerInfo(submit).getDivMap();
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

}


