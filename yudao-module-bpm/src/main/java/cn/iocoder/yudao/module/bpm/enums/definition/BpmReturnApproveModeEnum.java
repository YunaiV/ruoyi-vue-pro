package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * BPM 用户任务退回后的审批方式枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum BpmReturnApproveModeEnum implements ArrayValuable<Integer> {

    RESTART(1, "重新依次审批"), // 默认：流程跳转到退回的节点，从该节点重新依次审批
    RESUME_SOURCE(2, "从当前节点审批"); // 任务不跳转，退回节点的处理人修改表单后，回到当前节点继续审批。仅单人审批的节点生效，否则仍 RESTART

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BpmReturnApproveModeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    public static BpmReturnApproveModeEnum typeOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
