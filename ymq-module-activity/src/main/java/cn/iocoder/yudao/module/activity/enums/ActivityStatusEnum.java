package cn.iocoder.yudao.module.activity.enums;

/**
 * 活动状态机。状态流转见 docs/6-核心算法.md（活动接龙）。
 *
 * RECRUITING → MATCHED → PLAYING → FINISHED 是正常路径；
 * CANCELLED 是任何时间都可以从 RECRUITING 中止；
 * MATCHED → RECRUITING 是对阵生成后又有人退出导致回到接龙的回滚（Sprint 1 第 2 周再处理）。
 */
public final class ActivityStatusEnum {

    /** 接龙中 */
    public static final String RECRUITING = "RECRUITING";
    /** 已生成对阵表，开赛前 */
    public static final String MATCHED = "MATCHED";
    /** 比赛进行中 */
    public static final String PLAYING = "PLAYING";
    /** 已结束 */
    public static final String FINISHED = "FINISHED";
    /** 已取消 */
    public static final String CANCELLED = "CANCELLED";

    private ActivityStatusEnum() {}

}
