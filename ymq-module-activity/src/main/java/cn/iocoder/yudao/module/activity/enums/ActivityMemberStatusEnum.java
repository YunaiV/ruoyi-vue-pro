package cn.iocoder.yudao.module.activity.enums;

public final class ActivityMemberStatusEnum {

    /** 已报名（默认） */
    public static final String JOINED = "JOINED";
    /** 候补（人数满后报名） */
    public static final String WAITING = "WAITING";
    /** 自行退出 */
    public static final String QUIT = "QUIT";
    /** 召集人移除 */
    public static final String REMOVED = "REMOVED";
    /** 报名但未到场 */
    public static final String NO_SHOW = "NO_SHOW";

    private ActivityMemberStatusEnum() {}

}
