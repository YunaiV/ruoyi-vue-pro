package cn.iocoder.yudao.module.ai.enums.requestlog;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 请求日志状态的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiRequestLogStatusEnum implements ArrayValuable<Integer> {

    SUCCESS(20, "成功"),
    FAIL(30, "失败"),
    CANCEL(40, "取消");

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiRequestLogStatusEnum::getStatus).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
