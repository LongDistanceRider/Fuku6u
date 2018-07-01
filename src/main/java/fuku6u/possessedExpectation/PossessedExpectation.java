package fuku6u.possessedExpectation;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 狂人予想
 */
public class PossessedExpectation {

    /* 狂人疑い */
    Map<Agent, Integer> posessedSuspectMap = new HashMap<>();
    /* 灰色boolean */
    Map<Agent, Boolean> suspectMap = new HashMap<>();
    /* 重複計算の回避リスト */
    List<String> flagList = new ArrayList<>();

    /**
     * 容疑が晴れる
     * @param agent
     */
    public void clearAgent(Agent agent) {
        suspectMap.put(agent, Boolean.FALSE);
    }

    public PossessedExpectation(GameInfo gameInfo) {
        List<Agent> agentList = gameInfo.getAliveAgentList();   // 参加エージェント
        agentList.remove(gameInfo.getAgent()); // 自分自身は除く
        agentList.forEach(agent -> {
            posessedSuspectMap.put(agent, 0);
            suspectMap.put(agent, Boolean.TRUE);
        });  // 狂人疑いマップを更新
    }

    /**
     * エージェントの疑い度を更新
     * @param agent
     *  更新するエージェント
     * @param addDistrust
     *  追加する疑い度
     */
    public void addAgentSuspect(Agent agent, int addDistrust) {
        // 同じ箇所から同じ情報を複数加算することを回避するために，フラグ処理を施す
        StackTraceElement[] stackTraceElements = (new Throwable()).getStackTrace(); // スタックトレースより呼び出し元情報の取り出し
        String methodName = stackTraceElements[1].getMethodName();
        String className = stackTraceElements[1].getClassName();
        int line = stackTraceElements[1].getLineNumber();

        String flagString = className + methodName + line + agent + addDistrust; // フラグ名作成
        if (!flagList.contains(flagString)) {
            int preDistrust = posessedSuspectMap.get(agent);
            int distrust = preDistrust + addDistrust;
            posessedSuspectMap.put(agent, distrust);
            flagList.add(flagString);
        }
    }
}
