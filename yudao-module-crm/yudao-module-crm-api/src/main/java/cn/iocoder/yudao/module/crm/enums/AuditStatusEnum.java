package cn.iocoder.yudao.module.crm.enums;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;

import java.util.Arrays;

/**
 * 流程审批状态枚举类
 * 0 未审核 1 审核通过 2 审核拒绝 3 审核中 4 已撤回
 * @author 赤焰
 */
public enum AuditStatusEnum implements IntArrayValuable {
    /**
     * 未审批
     */
    AUDIT_NEW(0, "未审批"),
    /**
     * 审核通过
     */
	AUDIT_FINISH(0, "审核通过"),
    /**
     * 审核拒绝
     */
	AUDIT_REJECT(2, "审核拒绝"),
    /**
     * 审核中
     */
    AUDIT_DOING(3, "审核中"),
	/**
	 * 已撤回
	 */
	AUDIT_RETURN(4, "已撤回");

    private final Integer value;
    private final String desc;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(AuditStatusEnum::getValue).toArray();

    /**
     *
     * @param value
     * @param desc
     */
    AuditStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }
}
