package cn.iocoder.yudao.module.ai.enums.ocr;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 文字识别状态的枚举
 *
 * 识别是同步的，记录只在识别结束后写入，所以没有“识别中”
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiOcrStatusEnum implements ArrayValuable<Integer> {

    SUCCESS(20, "识别成功"),
    FAIL(30, "识别失败");

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiOcrStatusEnum::getStatus).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
