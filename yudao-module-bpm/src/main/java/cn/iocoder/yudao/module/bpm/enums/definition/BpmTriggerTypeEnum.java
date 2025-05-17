package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * BPM Simple 触发器类型枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmTriggerTypeEnum implements ArrayValuable<Integer> {

    HTTP_REQUEST(1, "发起 HTTP 请求"), // BPM => 业务，流程继续执行，无需等待业务
    HTTP_CALLBACK(2, "接收 HTTP 回调"), // BPM => 业务 => BPM，流程卡主，等待业务回调

    FORM_UPDATE(10, "更新流程表单数据"),
    FORM_DELETE(11, "删除流程表单数据"),
    ;

    /**
     * 触发器执行动作类型
     */
    private final Integer type;

    /**
     * 触发器执行动作描述
     */
    private final String desc;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BpmTriggerTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static BpmTriggerTypeEnum typeOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }
}
