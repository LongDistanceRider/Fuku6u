package fuku6u.player.nl;

import fuku6u.log.Log;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.apache.lucene.search.spell.LevensteinDistance;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自然言語部門用
 * 自然言語からプロトコルへ変換作業
 *
 * 1 文を分ける
 * 2 フィルターにかける
 * 3 一般化
 */
public class NlProcessing {

    /* PATH */
    private static final String dir = System.getProperty("user.dir");
    /* 照合する際に用いるレーベンシュタイン距離の閾値 */
    private static final double DISTANCE_THRESHOLD = 0.5;
    /* フィルタ情報 */
    private static List<String> filterList = new ArrayList<>();
    /* 照合ファイル */
    private static Map<String, String[]> comparisonMap = new HashMap<>();
    /* タグファイル */
    private static Map<String, String[]> tagMap = new HashMap<>();
    /* submitのAgent */
    private Agent submitAgent;
    /* submitがCOした役職 */
    private Role submitCoRole;

    static {
        // フィルタ情報の読み込み
        try {
            File csv = new File(dir + "/lib/filter-info.txt");

            BufferedReader bufferedReader = new BufferedReader(new FileReader(csv));
            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                filterList.add(readLine.trim());    // 制御文字と空白を削除してからリストへ追加
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            Log.fatal("フィルタ情報読み込みでエラー" + e);
        } catch (IOException e) {
            Log.fatal("フィルタ情報読み込みでエラー" + e);
        }
        // 照合ファイルの読み込み
        try(BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(dir +"/lib/comparison.csv"))) {
            List<String> comparisonList = new ArrayList<>();
            String readLine = "";
            while((readLine = bufferedReader.readLine()) != null) {
                comparisonList.add(readLine.trim());    // 制御文字と空白を削除してからリストへ追加
            }

            for (String line :
                    comparisonList) {
                String[] arrayLine = line.split(",");
                comparisonMap.put(arrayLine[0], Arrays.copyOfRange(arrayLine, 1, arrayLine.length));
            }
        } catch (IOException e) {
            Log.fatal("照合ファイルの読み込みでエラー" + e);
        }
        // タグファイルの読み込み
        try(BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(dir + "/lib/tag.csv"))) {
            List<String> tagList = new ArrayList<>();
            String readLine = "";
            while((readLine = bufferedReader.readLine()) != null) {
                tagList.add(readLine.trim());    // 制御文字と空白を削除してからリストへ追加
            }

            for (String line :
                    tagList) {
                String[] arrayLine = line.split(",");
                tagMap.put(arrayLine[0], Arrays.copyOfRange(arrayLine, 1, arrayLine.length));
            }
        } catch (IOException e) {
            Log.fatal("タグファイルの読み込みでエラー" + e);
        }
    }

    public NlProcessing(Agent submitAgent, Role submitCoRole) {
        this.submitAgent = submitAgent;
        this.submitCoRole = submitCoRole;
    }

    /**
     * 自然言語からプロトコル文への変換
     * @param talkText
     * @return
     */
    public List<String> convert(String talkText) {
        // 返すプロトコル文
        List<String> protocolTextList = new ArrayList<>();

        // Text構造体作成
        Text text = new Text(talkText);

        // 1文ずつ処理
        for (String sentence :
                text.getSentence()) {
            // STEP 2: フィルタにかける「わーい」とか「頑張るぞー」とかは解析不要のため，コンティニュー
            if (sentence.equals("") || sentence.equals("Over")
                    || sentence.equals("Skip") || isChat(sentence)) {
                // 解析する必要のない発話を除外（中身のない発言，Over・Skip発言，雑談
                Log.trace("NL解析不要: " + sentence);
                continue;
            }
            // 句点までの内容にフィルターが引っかからない場合は句点までの文を削除する．
            String[] textArray = sentence.split("、");
            for (int i = 0; i < textArray.length; i++) {
                if (isChat(textArray[i])) {
                    textArray[i] = "";
                }
            }
            sentence = String.join("", textArray);

            // STEP 3: 文をタグ変換（<TARGET>を除く）
            String convertToTag = sentence;
            for (Map.Entry<String, String[]> tagEntry:
                    tagMap.entrySet()) {
                int index;
                String tmpText = sentence;  // 削除されながら走査される文字列
                while (true) {
                    index = tmpText.indexOf(tagEntry.getKey());
                    if (index != -1) {
                        text.addTagEntryList(tagEntry, sentence, index);  // 変換を保存
                        tmpText = tmpText.substring(index + tagEntry.getKey().length());    // 変換したとこまでの文字列を削除して再走査
                    } else {
                        break;
                    }
                }
                convertToTag = convertToTag.replaceAll(tagEntry.getKey(), tagEntry.getValue()[0]); // タグ文字に変換
            }

            // タグ変換<TARGET>をする
            // -- "Agent["を走査してその後の"]"までを保存する 「Agent[01]は人狼」から「Agent[01]」を取り出す
            String tmpText = convertToTag;   // 削除されながら走査される文字列
            while(true) {
                int index = tmpText.indexOf("Agent[");
                if (index != -1) {
                    int endIndex = tmpText.indexOf("]");
                    text.addTargetList((String)tmpText.subSequence(index, endIndex+1));
                    tmpText = tmpText.substring(endIndex+1);
                } else {
                    break;
                }
            }
            Pattern pattern = Pattern.compile("Agent\\[[0-9]{2}]");
            Matcher matcher = pattern.matcher(convertToTag);
            convertToTag = matcher.replaceAll("<TARGET>");  // タグ文字に変換

            Log.trace("照合前比較文: " + convertToTag);
            // 照合
            double maxDistance = 0; // 最大ユークリッド距離（一番近い距離が1，遠い距離が0のdouble型
            Map.Entry<String, String[]> maxComparisonEntry = null;
            for (Map.Entry<String, String[]> comparisonEntry :
                    comparisonMap.entrySet()) {
                LevensteinDistance levensteinDistance = new LevensteinDistance();
                double distance = levensteinDistance.getDistance(comparisonEntry.getKey(), convertToTag);
                if (distance > maxDistance) {
                    maxDistance = distance;
                    maxComparisonEntry = comparisonEntry;
                }
            }

            Log.trace("最大ユークリッド距離獲得照合ファイル文: " + maxComparisonEntry.getKey() + " 距離: " + maxDistance);
            // 距離がDISTANCE_THRESHOLD以下は変換不可能とする
            if (maxDistance < DISTANCE_THRESHOLD) {
                Log.trace("ユークリッド距離不足:: 距離: " + maxDistance + " sentence: " + sentence + " convertToTag: " + convertToTag);
                continue;
            }
            // 照合ファイルから話題を取って来た後，各話題の処理を行う
            String[] topics = maxComparisonEntry.getValue();    // ex: DIVINED,0,0,ESTIMATE,1,1
            for (int i = 0; i < topics.length; i += 3) {
                String target = null;
                Role role = null;
                Species species = null;
                switch (topics[i]) {
                    case "COMINGOUT":
                        // <ROLE>照合
                        role = getRole(text, Integer.parseInt(topics[i+1]));
                        if (role != null) {
                            protocolTextList.add("COMINGOUT " + submitAgent + " " + role);   // プロトコル文変換
                        } else {
                            Log.warn("Role型がnullのため変換に失敗しました．sentence: " + sentence + " role: " + role);
                            break;
                        }
                        break;
                    case "DIVINED":
                        // 霊能者COしている場合はIDENに変更する
                        // <TARGET>照合
                        target = getTargetString(text, Integer.parseInt(topics[i+1]));
                        if (target == null) {
                            Log.warn("DIVINED変換中に予期しないエラー（null）が発生しました．sentence: " + sentence);
                            break;
                        }
                        // <ROLE>からSPECIES照合
                        species = getSpecies(text, Integer.parseInt(topics[i+2]));
                        if (species!= null) {
                            // submitがCOした役職＝
                            //  占い師ならばDIVINED，霊能者ならばIDEN
                            if (submitCoRole.equals(Role.SEER)) {
                                protocolTextList.add("DIVINED " + target + " " + species);
                            } else if (submitCoRole.equals(Role.MEDIUM)) {
                                protocolTextList.add("IDENTIFIED " + target + " " + species);
                            } else {
                                Log.warn("発言者がCOした役職がわからなかったため，ESTIMATEに強制変換しました．");
                                protocolTextList.add("ESTIMATE " + target + " " + species);
                            }
                        }
                        break;
                    case "ESTIMATE":
                        // <TARGET>照合
                        target = getTargetString(text, Integer.parseInt(topics[i+1]));
                        if (target == null) {
                            Log.warn("DIVINED変換中に予期しないエラー（null）が発生しました．talkText: " + text.getTalkText());
                            break;
                        }
                        // <ROLE>照合
                        role = getRole(text, Integer.parseInt((topics[i+2])));
                        if (role != null) {
                            protocolTextList.add("ESTIMATE " + target + " " + role);
                        } else {
                            Log.warn("DIVINED変換中に予期しないエラー（null）が発生しました．talkText: " + text.getTalkText());
                            break;
                        }
                        break;
                    case "VOTE":
                        // <TARGET>照合
                        target = getTargetString(text, Integer.parseInt(topics[i+1]));
                        if (target != null) {
                            protocolTextList.add("VOTE " + target);
                        } else {
                            Log.warn("VOTE変換中に予期しないエラー（null）が発生しました．talkText: " + text.getTalkText());
                            break;
                        }
                        break;
                    case "REQUEST_VOTE" :
                        // <TARGET>照合
                        target = getTargetString(text, Integer.parseInt(topics[i+1]));
                        if (target != null) {
//                            protocolTextList.add("REQUEST VOTE " + target);   // REQUESTの書き方がわからないので，コメントアウトしておく
                        } else {
                            Log.warn("REQUEST_VOTE変換中に予期しないエラー（null）が発生しました．talkText: " + text.getTalkText());
                            break;
                        }
                        break;
                    case "IMPOSSIBLE":
                        // <TARGET>照合
                        target = getTargetString(text, Integer.parseInt(topics[i+1]));
                        if (target != null) {
                            // TODO NL話題の処理をここに書く
                        } else {
                            Log.warn("IMPOSSIBLE変換中に予期しないエラー（null）が発生しました．talkText: " + text.getTalkText());
                            break;
                        }
                        break;
                    case "LIAR":
                        // <TARGET>照合
                        target = getTargetString(text, Integer.parseInt(topics[i+1]));
                        if (target != null) {
                            // TODO NL話題の処理をここに書く
                        } else {
                            Log.warn("LIAR変換中に予期しないエラー（null）が発生しました．talkText: " + text.getTalkText());
                            break;
                        }
                        break;
                    case "SUSPICIOUS":
                        // <TARGET>照合
                        target = getTargetString(text, Integer.parseInt(topics[i+1]));
                        if (target != null) {
                            // TODO NL話題の処理をここに書く
                        } else {
                            Log.warn("SUSPICIOUS変換中に予期しないエラー（null）が発生しました．talkText: " + text.getTalkText());
                            break;
                        }
                        break;
                    case "TRUST":
                        // <TARGET>照合
                        target = getTargetString(text, Integer.parseInt(topics[i+1]));
                        if (target != null) {
                            // TODO NL話題の処理をここに書く
                        } else {
                            Log.warn("TRUST変換中に予期しないエラー（null）が発生しました．talkText: " + text.getTalkText());
                            break;
                        }
                        break;
                    default:
                        Log.warn("想定していないSwitch-defaultに分岐しました．sentence: " + sentence + " topics[i]" + topics[i]);
                        break;

                }
            }


        }


        return protocolTextList;
    }

    /**
     * Speciesを取得
     * @param text
     * @param number
     *          何番目に出現したROLEタグを取得するか
     * @return
     */
    private Species getSpecies (Text text, int number) {
        Species species = null;
        String speciesString = getRoleOrSpecies(text, number, 2);

        if (speciesString != null) {
        try {
            species = Species.valueOf(speciesString);
        } catch (IllegalArgumentException e) {
            Log.warn("存在しないSpecies型に変換しようとして失敗しました．talkText: " + text.getTalkText());
        }
        } else {
            Log.warn("変換文からRoleに変換できる単語が発見されませんでした．talkText: " + text.getTalkText());
        }
        return species;
    }
    /**
     * Roleを取得
     * @param text
     * @param number
     *          照合ファイルの4列目を指定する場合，1列目はKey担っているため，Valueが2列目から始まる．２を指定すると4列目の数字を取ってくる
     *          大抵は2（というか2しかないはず）
     * @return
     */
    private Role getRole (Text text, int number) {
        Role role = null;
        String speciesString = getRoleOrSpecies(text, number, 1);

        if (speciesString != null) {
            try {
                // Role型に変換
                role = Role.valueOf(speciesString);
            } catch (IllegalArgumentException e) {
                Log.warn("存在しないRole型に変換しようとして失敗しました．talkText: " + text.getTalkText());
            }
        } else {
            Log.warn("変換文からRoleに変換できる単語が発見されませんでした．talkText: " + text.getTalkText());
        }
        return role;
    }

    private String getRoleOrSpecies (Text text, int number, int cast) {
        String string = null;
        List<String> tagStringListCo = text.getTagStringList("<ROLE>",cast);
        if (!tagStringListCo.isEmpty()) {
            try {
                string = tagStringListCo.get(number);   // tagString = SEER | WEREWOLF |　などなど．．．
            } catch(IndexOutOfBoundsException e) {
                Log.warn("存在しないインデックスを取得しようとして失敗しました．talkText: " + text.getTalkText());
            }
        } else {
            Log.trace("COMINGOUT変換中に<ROLE>タグが見つからず，解析終了．talkText:" + text.getTalkText());
        }
        return string;

    }

    private String getTargetString (Text text, int number) {
        String targetString = null;
        List<String> targetList = text.getTargetList();
        if (!targetList.isEmpty()) {
            try {
                targetString = targetList.get(number);
            } catch(IndexOutOfBoundsException e) {
                Log.warn("存在しないインデックスを取得しようとして失敗しました．talkText: " + text.getTalkText());
            }
        } else {
            Log.trace("DIVINED変換中に<TARGET>タグが見つからず，解析終了．talkText: " + text.getTalkText());
        }
        return targetString;
    }

    private static boolean isChat(String text) {
        for (String filter : filterList) {
            if (text.indexOf(filter) != -1) {
                return false;
            }
        }
        return true;
    }


}
