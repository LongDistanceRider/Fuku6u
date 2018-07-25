package fuku6u.utterance;

import fuku6u.flag.Flag;
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
    public void offer(Topic topic, Agent target, Species species, String nlString) {
        if (Flag.isNL()) {
            offer(nlString);
        } else {
            switch (topic) {
                case DIVINED:
                    ContentBuilder builder = new DivinedResultContentBuilder(target, species);
                    offer(new Content(builder).getText());
                    return;
            }
        }
    }

    /**
     * プロトコル発言生成を担うメソッド
     * @param topic
     * @param target
     * @param role
     */
    public void offer(Topic topic, Agent target, Role role, String nlString) {
        if (Flag.isNL()) {
            offer(nlString);
        } else {
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
    }
    /**
     * プロトコル発言生成を担うメソッド
     * @param topic
     * @param target
     */
    public void offer(Topic topic, Agent target, String nlString) {
        if (Flag.isNL()) {
            offer(nlString);
        } else {
            switch (topic) {
                case VOTE:
                    ContentBuilder builder = new VoteContentBuilder(target);
                    offer(new Content(builder).getText());
                    return;
            }
        }
    }

    public void offer(Topic topic, TalkType talkType, int day, int id, String nlString) {
        if (Flag.isNL()) {
            offer(nlString);
        } else {
            switch (topic) {
                case AGREE:
                    ContentBuilder builder = new AgreeContentBuilder(talkType, day, id);
                    offer(new Content(builder).getText());
                    return;
                case DISAGREE:
                    ContentBuilder builder2 = new DisagreeContentBuilder(talkType, day, id);
                    offer(new Content(builder2).getText());
                    return;
            }
        }
    }

    /**
     * キューに発言を追加
     * @param utterance
     *  追加したい発言
     */
    public void offer(String utterance) {
        // 空文字は処理しない
        if (utterance == "") {
            return;
        }
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

    /**
     * Speciesを自然言語に変換
     * @param species
     * @return
     */
    public static String convertSpeciesToNl (Species species) {
        if (species.equals(Species.HUMAN)) {
            return "人間";
        }
        return "人狼";
    }

    /**
     * Roleを自然言語に変換
     */
    public static String convertRoleToNl (Role role) {
        switch (role) {
            case SEER:
                return "占い師";
            case MEDIUM:
                return "霊能者";
            case BODYGUARD:
                return "狩人";
            case POSSESSED:
                return "裏切り者";
            case WEREWOLF:
                return "人狼";
            default:
                return "村人";
        }
    }
}
