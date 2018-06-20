package fuku6u.wolfGroupExpectation;

/**
 * 人狼グループ推定に用いるパラメータの保持，更新
 * 値は動的に変化するためenumにはしない予定
 *
 */
public class WolfGroupParameter {
    private static int conviction_PoseWolf = 1000;    // 狂狼を確信

    private static int unlikely_Wolf = -100;   // 人狼っぽくない

    public static int getConviction_PoseWolf() {
        return conviction_PoseWolf;
    }

    public static int getUnlikely_Wolf() {
        return WolfGroupParameter.unlikely_Wolf;
    }
}
