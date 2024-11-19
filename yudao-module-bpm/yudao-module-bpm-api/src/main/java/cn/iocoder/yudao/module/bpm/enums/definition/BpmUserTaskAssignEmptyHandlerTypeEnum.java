package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * BPM 用户任务的审批人为空时，处理类型枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum BpmUserTaskAssignEmptyHandlerTypeEnum implements IntArrayValuable {

    APPROVE(1), // 自动通过
    REJECT(2), // 自动拒绝
    ASSIGN_USER(3), // 指定人员审批
    ASSIGN_ADMIN(4), // 转交给流程管理员
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BpmUserTaskAssignEmptyHandlerTypeEnum::getType).toArray();

    private final Integer type;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
