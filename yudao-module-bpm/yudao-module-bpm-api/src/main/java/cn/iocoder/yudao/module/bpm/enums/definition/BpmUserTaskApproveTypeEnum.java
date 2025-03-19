package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用户任务的审批类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum BpmUserTaskApproveTypeEnum implements ArrayValuable<Integer> {

    USER(1), // 人工审批
    AUTO_APPROVE(2), // 自动通过
    AUTO_REJECT(3); // 自动拒绝

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BpmUserTaskApproveTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
