package cn.iocoder.yudao.module.ai.enums.write;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 写作语言的枚举，与字典 ai_write_language 一致
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiWriteLanguageEnum implements ArrayValuable<Integer> {

    AUTO(1, "自动"),
    CHINESE(2, "中文"),
    ENGLISH(3, "英文"),
    KOREAN(4, "韩语"),
    JAPANESE(5, "日语");

    /**
     * 语言
     */
    private final Integer language;
    /**
     * 名字
     */
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiWriteLanguageEnum::getLanguage).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
