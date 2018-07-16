package fuku6u.player;

import fuku6u.Expectation.PossessedExpectation;
import fuku6u.Expectation.WolfGroupExpectation;
import fuku6u.board.BoardSurface;
import fuku6u.board.Util;
import fuku6u.log.Log;
import fuku6u.observer.DayStartObserver;
import fuku6u.observer.TalkEndObserver;
import fuku6u.observer.TalkObserver;
import fuku6u.player.nl.NlProcessing;
import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import java.util.ArrayList;
import java.util.List;

public class Fuku6u implements Player {

    /* NLスイッチ */
    private boolean isNl = true;
    /* ゲーム情報 */
    private GameInfo gameInfo;
    /* 盤面 */
    private BoardSurface boardSurface;
    /* 人狼グループ */
    private WolfGroupExpectation wExpect;
    /* 狂人予想 */
    private PossessedExpectation pExpect;
    /* finish()フラグ */
    private boolean isFinish = false;
    /* トークリストをどこまで読み込んだか */
    private static int talkListHead = 0;

    @Override
    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
        Log.debug("initialize()実行");
        this.gameInfo = gameInfo;
        boardSurface = new BoardSurface(gameInfo);
        wExpect = new WolfGroupExpectation(gameInfo);
        pExpect = new PossessedExpectation();
        isFinish = false;
    }

    @Override
    public void update(GameInfo gameInfo) {
        Log.debug("update()実行");
        this.gameInfo = gameInfo;
        // 発言処理
        talkProcessing();
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
                // 役職セット
                boardSurface.setAssignRole(gameInfo);
                // 役職固有の処理
                boardSurface.getAssignRole().dayStart(gameInfo, boardSurface, wExpect);
                break;
            default: // 2日目以降
                // 被投票者
                Agent executedAgent = gameInfo.getExecutedAgent();
                boardSurface.executedAgent(executedAgent);  // 追放されたエージェントを保管
                Log.info("追放者: " + executedAgent);
                // 被害者
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
                // 役職固有の処理
                boardSurface.getAssignRole().dayStart(gameInfo, boardSurface, wExpect);
                // 観測クラスの実行
                DayStartObserver observer = new DayStartObserver(gameInfo, boardSurface, wExpect, pExpect);
                observer.check(attackedAgent);
        }
    }

    @Override
    public String talk() {
        Log.debug("talk()実行");
        boardSurface.getAssignRole().talk(boardSurface);    // 役職としての発言（CO，占い結果発言，霊能結果発言など）
        String talk = Utterance.getInstance().poll();
        if (talk != null) {
            return talk;
        }
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

        TalkEndObserver talkEndObserver = new TalkEndObserver(gameInfo, boardSurface, wExpect, pExpect);
        talkEndObserver.check();

        // voteは格役職毎に処理を変えるため，.roleへ処理を移行し，投票先を受け取る
        List<Agent> candidateAgentList = gameInfo.getAliveAgentList();
        candidateAgentList.remove(boardSurface.getMe());
        List<Agent> votedAgentList = boardSurface.getAssignRole().vote(candidateAgentList, wExpect, pExpect);

        Agent votedAgent = Util.randomElementSelect(votedAgentList);
        Log.info("投票先: " + votedAgent);
        return votedAgent;
    }

    @Override
    public Agent attack() {
        Log.debug("attack()実行");
        // TODO 投票噛みは回避できる？
        List<Agent> candidateAgentList = new ArrayList<>();

        // 狂狼でないことが確定している人をattackする
        List<Agent> possessedClearAgent = pExpect.getClearAgentList();
        if (!possessedClearAgent.isEmpty()) {
            candidateAgentList.addAll(possessedClearAgent);
        }
        List<Agent> werewolfClearAgent = wExpect.getClearAgentList();
        if (!werewolfClearAgent.isEmpty()) {
            candidateAgentList.addAll(werewolfClearAgent);
        }
        candidateAgentList.removeAll(boardSurface.getWerewolfList());   // 仲間にattackしないように

        if (!candidateAgentList.isEmpty()) {
            Agent attackedAgent = Util.randomElementSelect(candidateAgentList);
            Log.info("襲撃先: " + attackedAgent);
            return attackedAgent;
        }

        // 狂人の可能性が低いエージェントにアタック
        List<Agent> checkAgentList = new ArrayList<>();
        checkAgentList.addAll(gameInfo.getAliveAgentList());
        checkAgentList.remove(boardSurface.getMe());
        checkAgentList.removeAll(boardSurface.getWerewolfList());

        int min_distrust = 1000;
        for (Agent candidateAgent :
                checkAgentList) {
            int distrustValue = pExpect.getAgentDistrust(candidateAgent);
            if (distrustValue < min_distrust) {
                candidateAgentList.clear();
                min_distrust = distrustValue;
            }
            if (distrustValue == min_distrust) {
                candidateAgentList.add(candidateAgent);
            }
        }

        if (!candidateAgentList.isEmpty()) {
            Agent attackedAgent = Util.randomElementSelect(candidateAgentList);
            Log.info("襲撃先: " + attackedAgent);
            return attackedAgent;
        }

        return null;
    }

    @Override
    public Agent divine() {
        Log.debug("divine()実行");
        List<Agent> candidateAgentList = boardSurface.getCandidateDivinedAgentList();
        // 人狼の可能性が高いエージェントを占う
        List<Agent> maxDistrustAgents = wExpect.getMaxDistrustAgent(candidateAgentList);
        if (!maxDistrustAgents.isEmpty()) {
            return Util.randomElementSelect(maxDistrustAgents);
        }
        // 生存プレイヤの中から狂人の可能性が高いエージェントを除き，適当に選択する
        maxDistrustAgents = pExpect.getMaxDistrustAgent(candidateAgentList);
        if (!maxDistrustAgents.isEmpty()) {
            candidateAgentList.removeAll(pExpect.getMaxDistrustAgent(candidateAgentList));
        }
        return Util.randomElementSelect(candidateAgentList);
    }

    @Override
    public Agent guard() {
        Log.debug("guard()実行");
        // 確定占い師が存在する場合は占い師をguardする
        // 確定霊能者が存在する場合は霊能者をguardする
        // 占い師の中から人狼の可能性が低い方を選択する
        // 霊能者の中から人狼の可能性が低い方を選択する
        // 生存者の中から人狼の可能性が低いagentを選択する
        // 生存者の中から適当に護衛先を選択する
        // TODO ガード先は○-○進行によって変える必要がある．とりあえず適当に処理書いておく
        List<Agent> candidates = gameInfo.getAliveAgentList();
        candidates.remove(boardSurface.getMe());
        Agent guardedAgent = Util.randomElementSelect(candidates);
        Log.info("護衛先: " + guardedAgent);
        return guardedAgent;
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
            if (boardSurface.getAssignRole().getRole().equals(Role.WEREWOLF)) {
                Log.info("勝ち");
            } else {
                Log.info("負け");
            }
        } else {
            Log.info("勝敗結果: 村人陣営 勝利");
            if (boardSurface.getAssignRole().getRole().equals(Role.WEREWOLF)) {
                Log.info("負け");
            } else {
                Log.info("勝ち");
            }
        }

        // ----- デバックログ出力開始 -----

        // -----  -----
        isFinish = true;
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

    private void talkProcessing() {
        List<Talk> talkList = gameInfo.getTalkList();
        for (int i = talkListHead; i < talkList.size(); i++) {
            Talk talk = talkList.get(i);
            Log.info("Taker: " + talk.getAgent() + " mes: " + talk.getText());
            if (talk.getAgent().equals(boardSurface.getMe())) {  // 自分自身の発言はスキップ
                continue;
            }
            // Talkを保管
            boardSurface.addTalk(talk);
            //TODO NL処理を噛ませる
            List<String> protocolTextList = new ArrayList<>();
            if (isNl) {
                // NL処理をかませる
                NlProcessing nlProcessing = new NlProcessing(talk.getAgent(), boardSurface.getCoRole(talk.getAgent()));
                protocolTextList = nlProcessing.convert(talk.getText());
            } else {
                protocolTextList.add(talk.getText());
            }
            for (String protocolText :
                    protocolTextList) {
                Log.info("ProtocolText: " + protocolText);
                // String text を Contentに変換する
                Content content = new Content(protocolText);
                // ラベルごとに処理
                switch (content.getTopic()) {
            /* --- 意図表明に関する文 --- */
                    case COMINGOUT:
                        boardSurface.addComingoutRole(talk.getAgent(), content.getRole()); // CO役職を保管
                        TalkObserver.comingout(boardSurface, wExpect, pExpect, talk.getAgent(), content.getRole());
                        break;
                    case ESTIMATE:
                        break;
            /* --- 能力結果に関する文 --- */
                    case DIVINED:
                        boardSurface.addDivMap(talk.getAgent(), content.getTarget(), content.getResult()); // 占い結果を保管
                        TalkObserver.divined(boardSurface, wExpect, pExpect, talk.getAgent(), content.getTarget(), content.getResult());
                        break;
                    case IDENTIFIED:
                        boardSurface.addIdenMap(talk.getAgent(), content.getTarget(), content.getResult()); // 霊能結果を保管
                        break;
//                case GUARDED:
//                    break;
//            /* --- ルール行動・能力に関する文 --- */
//                case DIVINATION:
//                    break;
//                case GUARD:
//                    break;
                    case VOTE:
                        boardSurface.addVote(talk.getAgent(), content.getTarget()); // 投票先発言を保管
                        break;
//                case ATTACK:
//                    break;
//            /* --- 同意・非同意に関する文 --- */
//                case AGREE:
//                    break;
//                case DISAGREE:
//                    break;
//            /* --- 発話制御に関する文 --- */
//                case OVER:
//                    break;
//                case SKIP:
//                    break;
//            /* --- REQUEST文 --- */
//                case OPERATOR:
//                    break;
                    default:
                        break;
                }

            }
        }
        talkListHead = talkList.size();
    }
}
