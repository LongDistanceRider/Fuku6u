package fuku6u.wolfGroupExpectation;

/**
 * 人狼グループ推定に用いるパラメータの保持，更新
 * 値は動的に変化するためenumにはしない予定
 *
 */
public class WolfGroupParameter {
    private static int conviction_PoseWolf = 1000;    // 狂狼を確信

    private static int unlikely_Wolf = -100;   // 人狼っぽくない

    private static int likely_White = -50;  // 白より

    private static int likely_Black = 50; // 黒より

    public static int getConviction_PoseWolf() {
        return conviction_PoseWolf;
    }

    public static int getUnlikely_Wolf() {
        return WolfGroupParameter.unlikely_Wolf;
    }

    public static int getLikely_White() {
        return WolfGroupParameter.likely_White;
    }

    public static int getLikely_Black() {
        return WolfGroupParameter.likely_Black;
    }
}
