package fuku6u.possessedExpectation;

/**
 * 狂人推定に用いるパラメータ
 * 値の更新をするためenumにはしない
 */
public class PossessedParameter {
    private static int conviction_pose = 1000;    // 狂を確信
    private static int conviction_wolf = -1000;     // 人狼を確信

    private static int conviction_pose_wolf = 100; // ほぼ狂もしかしたら狼の可能性

    private static int may_pose = 10;   // 狂かもしれない

    public static int getConviction_pose() {
        return conviction_pose;
    }

    public static int getMay_pose() {
        return may_pose;
    }

    public static int getConviction_pose_wolf() {
        return conviction_pose_wolf;
    }

    public static int getConviction_wolf() {
        return conviction_wolf;
    }
}
