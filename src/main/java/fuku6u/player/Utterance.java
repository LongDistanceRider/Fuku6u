package fuku6u.player;

import com.sun.xml.internal.xsom.impl.Ref;
import fuku6u.log.Log;
import org.aiwolf.client.lib.*;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * 発言の一括管理クラス
 * すでに発言をしたかを管理し，何度も発言を繰り返すことを避ける
 * initialize-on-demand holder
 */
public class Utterance {

    /* すでに発言したかを管理するリスト */
    List<String> flagList = new ArrayList<>();
    /* 発言キュー */
    ArrayDeque<String> utteranceQueue = new ArrayDeque<>();

    /**
     * プロトコル発言生成を担うメソッド
     * @param topic
     * @param target
     * @param species
     */
    public void offer(Topic topic, Agent target, Species species) {
        switch (topic) {
            case DIVINED:
                ContentBuilder builder = new DivinedResultContentBuilder(target, species);
                offer(new Content(builder).getText());
                return;
        }
    }

    /**
     * プロトコル発言生成を担うメソッド
     * @param topic
     * @param target
     * @param role
     */
    public void offer(Topic topic, Agent target, Role role) {
        switch (topic) {
            case ESTIMATE:
                ContentBuilder builder = new EstimateContentBuilder(target, role);
                offer(new Content(builder).getText());
                return;
            case COMINGOUT:
                ContentBuilder builder2 = new ComingoutContentBuilder(target, role);
                offer(new Content(builder2).getText());
                return;
        }
    }
    /**
     * プロトコル発言生成を担うメソッド
     * @param topic
     * @param target
     */
    public void offer(Topic topic, Agent target) {
        switch (topic) {
            case VOTE:
                ContentBuilder builder = new VoteContentBuilder(target);
                offer(new Content(builder).getText());
                return;
        }
    }


    /**
     * キューに発言を追加
     * @param utterance
     *  追加したい発言
     */
    private void offer(String utterance) {
        // 同じ発言を何度もしないように，フラグ管理を施す
        StackTraceElement[] stackTraceElements = (new Throwable()).getStackTrace(); // スタックトレースより呼び出し元情報の取り出し
        String className = stackTraceElements[2].getClassName();    // クラス名取得
        String methodName = stackTraceElements[2].getMethodName();  // メソッド名取得
        int line = stackTraceElements[2].getLineNumber();   // 呼び出し元行数取得
        String flagString = className + methodName + line + utterance;  // フラグ名作成
        Log.trace("flagString" + flagString);

        // フラグチェック
        if (!flagList.contains(flagString)) {
            utteranceQueue.offer(utterance);
            flagList.add(flagString);
        }
    }

    /**
     * キューから発言を取り出し
     * @return キューが空の場合はnull
     */
    public String poll() {
        return utteranceQueue.poll();
    }

    /* *** initialize-on-demand holder *** */
    public static Utterance getInstance() {
        return UtteranceHolder.INSTANCE;
    }
    private Utterance(){}
    private static class UtteranceHolder {
        public static final Utterance INSTANCE = new Utterance();
    }
    /* *** *** */
}
