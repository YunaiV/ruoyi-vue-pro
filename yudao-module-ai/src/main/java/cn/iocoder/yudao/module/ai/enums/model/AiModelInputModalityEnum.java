package cn.iocoder.yudao.module.ai.enums.model;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * AI 模型输入模态的枚举
 *
 * 用于 {@link AiModelTypeEnum#CHAT} 对话模型，描述模型可以接收的输入类型，由用户按需配置
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum AiModelInputModalityEnum implements ArrayValuable<String> {

    TEXT("text", "文本"),
    IMAGE("image", "图片"),
    VIDEO("video", "视频");

    /**
     * 模态
     */
    private final String modality;
    /**
     * 模态名
     */
    private final String name;

    public static final String[] ARRAYS = Arrays.stream(values()).map(AiModelInputModalityEnum::getModality).toArray(String[]::new);

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /**
     * 判断是否支持该输入模态；为空时（历史数据），视为仅支持 text
     *
     * @param modalities 模型配置的输入模态数组
     * @param modality 输入模态
     * @return 是否支持
     */
    public static boolean isSupported(List<String> modalities, AiModelInputModalityEnum modality) {
        if (CollUtil.isEmpty(modalities)) {
            return modality == TEXT;
        }
        return modalities.contains(modality.getModality());
    }

}
