package cn.iocoder.yudao.module.ai.enums.requestlog;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 请求日志业务类型的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiRequestLogBizTypeEnum implements ArrayValuable<Integer> {

    CHAT(1, "聊天"),
    WRITE(2, "写作"),
    MIND_MAP(3, "思维导图");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiRequestLogBizTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
