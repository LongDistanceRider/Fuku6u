package fuku6u.player.nl;

import fuku6u.board.BoardSurface;
import fuku6u.log.Log;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.apache.lucene.search.spell.LevensteinDistance;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

    /* 照合する際に用いるレーベンシュタイン距離の閾値 */
    private static final double DISTANCE_THRESHOLD = 0.5;
    /* フィルタ情報 */
    private static List<String> filterList = new ArrayList<>();
    /* 照合ファイル */
    private static Map<String, String[]> comparisonMap = new HashMap<>();
    /* タグファイル */
    private static Map<String, String> tagMap = new HashMap<>();

    static {
        // フィルタ情報の読み込み
        try {
            File csv = new File("lib/filterInformation.txt");

            BufferedReader bufferedReader = new BufferedReader(new FileReader(csv));
            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                filterList.add(readLine);
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            Log.fatal("フィルタ情報読み込みでエラー" + e);
        } catch (IOException e) {
            Log.fatal("フィルタ情報読み込みでエラー" + e);
        }
        // 照合ファイルの読み込み
        try(BufferedReader bufferedReader = Files.newBufferedReader(Paths.get("lib/comparison.csv"))) {
            List<String> comparisonList = new ArrayList<>();
            String readLine = "";
            while((readLine = bufferedReader.readLine()) != null) {
                comparisonList.add(readLine);
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
        try(BufferedReader bufferedReader = Files.newBufferedReader(Paths.get("lib/tag.csv"))) {
            List<String> tagList = new ArrayList<>();
            String readLine = "";
            while((readLine = bufferedReader.readLine()) != null) {
                tagList.add(readLine);
            }

            for (String line :
                    tagList) {
                String[] arrayLine = line.split(",");
                tagMap.put(arrayLine[0], arrayLine[1]);
            }
        } catch (IOException e) {
            Log.fatal("タグファイルの読み込みでエラー" + e);
        }
    }
    public static String convert(BoardSurface bs, Talk talk) {
        // 文を分ける
        List<String> texts = separateText(talk.getText());
        for (String text :
                texts) {
            // フィルタにかける
            if (text.equals("") || text.equals("Over")
                    || text.equals("Skip") || isChat(text)) {
                // 解析する必要のない発話を除外（中身のない発言，Over・Skip発言，雑談
                Log.trace("NL解析不要: " + text);
                continue;
            }
            // 一般化　タグ変換する際に「人間」が<SPECIES>と<ROLE>の両方にいることに注意
            // ルール「句点までの内容にフィルターが引っかからない場合は句点までの文を削除する．
            String[] textArray = text.split("、");
            for (int i = 0; i < textArray.length; i++) {
                if (isChat(textArray[i])) {
                    textArray[i] = "";
                }
            }
            text = String.join("", textArray);
            // タグ変換<ROLE>をする
            String textTag = text.replaceAll("占い師", "<ROLE>");
            textTag = textTag.replaceAll("人狼", "<ROLE>");
            textTag = textTag.replaceAll("狂人", "<ROLE>");
            textTag = textTag.replaceAll("村人", "<ROLE>");
            textTag = textTag.replaceAll("狩人", "<ROLE>");
            textTag = textTag.replaceAll("霊能者", "<ROLE>");
            textTag = textTag.replaceAll("人間", "<ROLE>");
            textTag = textTag.replaceAll("白", "<ROLE>");
            textTag = textTag.replaceAll("黒", "<ROLE>");
            textTag = textTag.replaceAll("ぼく", "<FP>");
            textTag = textTag.replaceAll("僕", "<FP>");
            textTag = textTag.replaceAll("あたし", "<FP>");
            textTag = textTag.replaceAll("あたい", "<FP>");
            textTag = textTag.replaceAll("オレ", "<FP>");
            textTag = textTag.replaceAll("俺", "<FP>");
            textTag = textTag.replaceAll("わし", "<FP>");
            textTag = textTag.replaceAll("私", "<FP>");
            textTag = textTag.replaceAll("わたくし", "<FP>");

            // タグ変換<TARGET>をする
            Pattern pattern = Pattern.compile("Agent\\[[0-9]\\]{2}");
            Matcher matcher = pattern.matcher(textTag);
            text = matcher.replaceAll("<TARGET>");
            // 照合
            double maxDistance = 0;
            Map.Entry<String, String[]> maxComparisonEntry = null;
            for (Map.Entry<String, String[]> comparisonEntry :
                    comparisonMap.entrySet()) {
                LevensteinDistance levensteinDistance = new LevensteinDistance();
                double distance = levensteinDistance.getDistance(comparisonEntry.getKey(), textTag);
                if (distance < maxDistance) {
                    maxDistance = distance;
                    maxComparisonEntry = comparisonEntry;
                }
            }

            // 距離がDISTANCE_THRESHOLD以下は変換不可能とする
            if (maxDistance < DISTANCE_THRESHOLD) {
                continue;
            }
            // 照合ファイルから話題を取って来た後，各話題の処理を行う
            String[] topics = maxComparisonEntry.getValue();
            for (int i = 0; i < topics.length; i++) {
                switch (topics[i]) {
                    case "COMINGOUT":
                        // <ROLE>照合
                        for (Map.Entry<String, String> tagEntry :
                                tagMap.entrySet()) {
                            if (text.contains(tagEntry.getKey()) && tagEntry.getValue().equals("<ROLE>")) {
                                String roleString = tagEntry.getKey();

                            }
                        }
                        break;
                    case "DIVINED" :
                        break;
                    case "ESTIMATE" :
                        break;
                    case "VOTE":
                        break;
                    case "REQUEST" :
                        break;
                }
            }


        }


        return null;
    }

    private static boolean isChat(String text) {
        for (String filter : filterList) {
            if (text.indexOf(filter) != -1) {
                return false;
            }
        }
        return true;
    }

    private static List<String> separateText(String text) {
        String[] stringArray = text.split("[!.。！]");
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < stringArray.length; i++) {
            strings.add(stringArray[i]);
        }
        return strings;
    }
}
