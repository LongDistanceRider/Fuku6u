package fuku6u.wolfGroupExpectation;

/**
 * 人狼グループ推定に用いるパラメータの保持，更新
 * 値は動的に変化するためenumにはしない予定
 *
 */
public class WolfGroupParameter {
    private static int conviction_pose_wolf = 1000;    // 狂狼を確信

    private static int unlikely_were = -100;   // 人狼っぽくない

    public static int getConviction_pose_were() {
        return conviction_pose_wolf;
    }

    public static int getUnlikely_were() {
        return unlikely_were;
    }
}
