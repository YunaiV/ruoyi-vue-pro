package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 审批节点的操作按钮类型枚举
 * <p>
 * 与前端 SIMPLE 设计器的 OperationButtonType 枚举一致
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum BpmOperationButtonTypeEnum implements ArrayValuable<Integer> {

    APPROVE(1, "通过"),
    REJECT(2, "拒绝"),
    TRANSFER(3, "转办"),
    DELEGATE(4, "委派"),
    ADD_SIGN(5, "加签"),
    RETURN(6, "退回"),
    COPY(7, "抄送");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BpmOperationButtonTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
