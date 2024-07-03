package cn.iocoder.yudao.module.ai.enums.write;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum AiLanguageEnum implements IntArrayValuable {

    AUTO(1, "自动"),
    CHINESE(2, "中文"),
    ENGLISH(3, "英文"),
    KOREAN(4, "韩语"),
    JAPANESE(5, "日语");

    /**
     * Language code
     */
    private final Integer language;
    /**
     * Language name
     */
    private final String name;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(AiLanguageEnum::getLanguage).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

    public static AiLanguageEnum valueOfLanguage(Integer language) {
        for (AiLanguageEnum languageEnum : AiLanguageEnum.values()) {
            if (languageEnum.getLanguage().equals(language)) {
                return languageEnum;
            }
        }
        throw new IllegalArgumentException("未知语言： " + language);
    }

}