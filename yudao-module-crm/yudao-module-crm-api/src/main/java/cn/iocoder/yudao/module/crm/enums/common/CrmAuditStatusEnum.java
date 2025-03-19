package cn.iocoder.yudao.module.crm.enums.common;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * CRM 的审批状态
 *
 * @author 赤焰
 */
@RequiredArgsConstructor
@Getter
public enum CrmAuditStatusEnum implements ArrayValuable<Integer> {

    DRAFT(0, "未提交"),
    PROCESS(10, "审批中"),
    APPROVE(20, "审核通过"),
	REJECT(30, "审核不通过"),
    CANCEL(40, "已取消");

    private final Integer status;
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(CrmAuditStatusEnum::getStatus).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
