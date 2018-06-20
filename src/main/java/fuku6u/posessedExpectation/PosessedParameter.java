package fuku6u.posessedExpectation;

/**
 * 狂人推定に用いるパラメータ
 * 値の更新をするためenumにはしない
 */
public class PosessedParameter {
    private static int conviction_pose = 1000;    // 狂を確信

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
}
