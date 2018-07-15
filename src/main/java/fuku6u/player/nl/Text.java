package fuku6u.player.nl;

import org.aiwolf.common.data.Agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Text {

    private String talkText;
    private List<String> sentence;
    private String convertToTag;
    private List<Map.Entry<String, String[]>> tagEntryList = new ArrayList<>();
    private List<String> targetList = new ArrayList<>();

    /**
     * constracter
     *
     * @param talkText
     */
    public Text(String talkText) {
        this.talkText = talkText;
        this.sentence = separateText(talkText);
    }

    // Getter
    public List<String> getSentence() {
        return sentence;
    }

    public List<String> getTargetList() {
        return targetList;
    }

    public String getTalkText() {
        return talkText;
    }

    // Setter

    // Add
    public void addTagEntryList(Map.Entry<String, String[]> tagEntry) {
        this.tagEntryList.add(tagEntry);
    }

    public void addTargetList(String target) {
        this.targetList.add(target);
    }

    public List<String> getTagStringList (String tag, int cast) {
        List<String> reTagStringList = new ArrayList<>();
        tagEntryList.forEach((Entry -> {
            if (Entry.getValue()[0].equals(tag)) {// Topic
                reTagStringList.add(Entry.getValue()[cast]);
            }
        }));
        return reTagStringList;
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
