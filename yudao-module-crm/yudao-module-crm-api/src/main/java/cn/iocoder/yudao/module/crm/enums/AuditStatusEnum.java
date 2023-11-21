package cn.iocoder.yudao.module.crm.enums;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;

import java.util.Arrays;

// TODO @liuhongfeng：这个状态，还是搞成专属 CrmReceivableDO 专属的 status；
/**
 * 流程审批状态枚举类
 * 0 未审核 1 审核通过 2 审核拒绝 3 审核中 4 已撤回 TODO @liuhongfeng：这一行可以删除，因为已经有枚举属性了哈；
 * @author 赤焰
 */
// TODO @liuhongfeng：可以使用 @Getter、@AllArgsConstructor 简化 get、构造方法
public enum AuditStatusEnum implements IntArrayValuable {

    // TODO @liuhongfeng：草稿 0；10 审核中；20 审核通过；30 审核拒绝；40 已撤回；主要是留好间隙，万一每个地方要做点拓展； 然后，枚举字段的顺序调整下，审批中，一定要放两个审批通过、拒绝前面哈；
    /**
     * 未审批
     */
    AUDIT_NEW(0, "未审批"),
    /**
     * 审核通过
     */
	AUDIT_FINISH(1, "审核通过"),
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

    // TODO liuhongfeng：value 改成 status；desc 改成 name；
    private final Integer value;
    private final String desc;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(AuditStatusEnum::getValue).toArray();

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
