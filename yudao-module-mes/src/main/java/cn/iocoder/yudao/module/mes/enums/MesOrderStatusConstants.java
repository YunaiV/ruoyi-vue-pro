package cn.iocoder.yudao.module.mes.enums;

/**
 * MES 单据状态常量
 *
 * 集中管理各模块单据状态的编号，作为状态值的中央注册中心。
 * 各枚举类引用此处常量，避免硬编码数字。
 *
 * @author 芋道源码
 */
public final class MesOrderStatusConstants {

    /**
     * 草稿
     */
    public static final int PREPARE = 0;

    /**
     * 已确认
     */
    public static final int CONFIRMED = 1;
    /**
     * 审批中
     */
    public static final int APPROVING = 2;
    /**
     * 已审批
     */
    public static final int APPROVED = 3;

    /**
     * 已完成
     */
    public static final int FINISHED = 4;
    /**
     * 已取消
     */
    public static final int CANCELLED = 5;

}
