package fuku6u.player;

import fuku6u.board.BoardSurface;
import fuku6u.log.Log;
import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Fuku6u implements Player {

    /* ゲーム情報 */
    private GameInfo gameInfo;
    /* 盤面 */
    private BoardSurface boardSurface;
    /* 発言リスト */
    private static LinkedList<String> talkQueue = new LinkedList<>();
    /* finish()フラグ */
    private boolean isFinish = false;

    @Override
    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
        Log.debug("initialize()実行");
        this.gameInfo = gameInfo;
        boardSurface = new BoardSurface(gameInfo);
        isFinish = false;
    }

    @Override
    public void update(GameInfo gameInfo) {
        Log.debug("update()実行");
        this.gameInfo = gameInfo;
        // 発言処理
        TalkProcessing.update(gameInfo.getTalkList(), boardSurface);
    }

    @Override
    public void dayStart() {
        Log.debug("dayStart()実行");
        Log.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        Log.info("\t" + gameInfo.getDay() + "day start : My number is " + gameInfo.getAgent().toString());
        Log.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");

        switch (gameInfo.getDay()) {
            case 0: // 0日目
                return;
            case 1: // 1日目
                boardSurface.setAssignRole(gameInfo.getRole());  // 役職セット
                break;
            default: // 2日目以降
                // 被投票者
                Agent executedAgent = gameInfo.getExecutedAgent();
                boardSurface.executedAgent(executedAgent);  // 追放されたエージェントを保管
                Log.info("追放者: " + executedAgent);
                // 被噛み　null の場合はGJ発生
                Agent attackedAgent = null;
                for (Agent agent :
                        gameInfo.getLastDeadAgentList()) {  // 狐がいると2人返ってくると思われるため，この処理のままにしておく
                    Log.trace("getLastDeadAgentListで返ってくるAgent: " + agent);
                    if (!agent.equals(executedAgent)) {
                        attackedAgent = agent;
                    }
                }
                if (attackedAgent != null) {
                    boardSurface.attackedAgent(attackedAgent);
                    Log.info("被害者 : " + attackedAgent);
                } else {
                    Log.info("被害者 : なし（GJ発生）");
                }
                boardSurface.getAssignRole().dayStart(boardSurface);    // 役職固有の処理
        }

    }

    @Override
    public String talk() {
        Log.debug("talk()実行");
        boardSurface.getAssignRole().talk(boardSurface);    // 役職としての発言（CO，占い結果発言，霊能結果発言など）

        return Content.OVER.getText();
    }

    @Override
    public String whisper() {
        Log.debug("whisper()実行");
        return null;
    }

    @Override
    public Agent vote() {
        Log.debug("vote()実行");
        // TODO vote先の検討はまだ先
        // 黒判定されたエージェントがいれば追放
        List<Agent> blackDivinedAgentList = boardSurface.getBlackDivinedAgentList();
        if (!blackDivinedAgentList.isEmpty()) {
            return randomElementSelect(blackDivinedAgentList);
        }
        return null;
    }

    @Override
    public Agent attack() {
        Log.debug("attack()実行");
        // TODO 占いCOが1人ならアタック　15人の場合は別の人とか　ここも要検討
        return null;
    }

    @Override
    public Agent divine() {
        Log.debug("divine()実行");
        // TODO とりあえず占い候補から占う　意味のある占い候補を選ぶ必要がある
        List<Agent> candidateAgentList = boardSurface.getCandidateDivinedAgentList();
        if (!candidateAgentList.isEmpty()) {
            return randomElementSelect(candidateAgentList);
        }
        return null;
    }

    @Override
    public Agent guard() {
        Log.debug("guard()実行");
        // TODO ガード先は○-○進行によって変える必要がある．
        return null;
    }

    @Override
    public void finish() {
        Log.debug("finish()実行");
        // TODO 最新版では2度呼ばれなくなる．とりあえず2度呼ばれてもいいようにしておく
        if (isFinish) {  // finishが2回目に呼び出されるとき，処理をしない
            return;
        }

        // 役職finish()時の処理
        boardSurface.getAssignRole().finish(boardSurface);

        // 参加プレイヤのリザルト出力
        Map<Agent, Role> agentMap = gameInfo.getRoleMap();
        gameInfo.getRoleMap().forEach((agent, role) -> Log.info(agent + " Role: " + role));

        // 勝敗を出力
        boolean isWerewolfSideWin = false;
        for (Agent agent :
                gameInfo.getAliveAgentList()) {
            if (gameInfo.getRoleMap().get(agent).equals(Role.WEREWOLF)) {
                // 人狼勝利
                isWerewolfSideWin = true;
            }
        }
        if (isWerewolfSideWin) {
            Log.info("勝敗結果: 人狼陣営 勝利");
        } else {
            Log.info("勝敗結果: 村人陣営 勝利");
        }
        // ログ出力停止
        Log.endLog();
    }
    @Override
    public String getName() {
        Log.debug("getName()実行");
        return "Fuku6u";
    }

    /*
        private
     */

    /**
     * リストから要素を一つランダムに返す
     *
     * @param list
     * @param <T>
     * @return
     */
    private <T> T randomElementSelect(List<T> list) {
        if (list.isEmpty()) return null;
        else return list.get((int) (Math.random() * list.size()));
    }
}
