package cn.iocoder.yudao.module.ai.enums.music;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 音乐状态的枚举
 *
 * @author xiaoxin
 */
@AllArgsConstructor
@Getter
public enum AiMusicStatusEnum implements ArrayValuable<Integer> {

    IN_PROGRESS(10, "进行中"),
    SUCCESS(20, "已完成"),
    FAIL(30, "已失败");

    /**
     * 状态
     */
    private final Integer status;

    /**
     * 状态名
     */
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiMusicStatusEnum::getStatus).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
