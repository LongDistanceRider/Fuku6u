package fuku6u.flag;

import fuku6u.log.LogLevel;

/**
 * 実験時に使用するフラグの一括管理構造体クラス
 * 制約：
 *  private static finalのみ保持
 *  getterのみ実装
 */
public class Flag {

    /* 自然言語処理を行うか */
    private static final boolean nl = false;

    /* Logを書き出す最大レベル */
    private static final LogLevel maxLogLevel = LogLevel.WARN;

    public static boolean isNL() {
        return nl;
    }

    public static LogLevel getMaxLogLevel() {
        return maxLogLevel;
    }
}
