package fuku6u.board;

import java.util.List;

public class Util {
    /**
     * リストから要素を一つランダムに返す
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> T randomElementSelect(List<T> list) {
        if (list.isEmpty()) return null;
        else return list.get((int) (Math.random() * list.size()));
    }
}
