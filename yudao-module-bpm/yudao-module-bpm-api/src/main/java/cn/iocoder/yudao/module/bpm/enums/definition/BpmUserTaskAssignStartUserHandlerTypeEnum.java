package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * BPM 用户任务的审批人与发起人相同时，处理类型枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum BpmUserTaskAssignStartUserHandlerTypeEnum implements ArrayValuable<Integer> {

    START_USER_AUDIT(1), // 由发起人对自己审批
    SKIP(2), // 自动跳过【参考飞书】：1）如果当前节点还有其他审批人，则交由其他审批人进行审批；2）如果当前节点没有其他审批人，则该节点自动通过
    TRANSFER_DEPT_LEADER(3); // 转交给部门负责人审批【参考飞书】：若部门负责人为空，则自动通过

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BpmUserTaskAssignStartUserHandlerTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
