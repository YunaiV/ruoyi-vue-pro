package cn.iocoder.yudao.module.ai.enums.ppt;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI PPT 素材来源的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiPptSourceEnum implements ArrayValuable<Integer> {

    /**
     * 粘贴大纲：用户原文直接作为分镜输入
     */
    PASTE(1, "粘贴大纲"),
    /**
     * 上传材料：生成任务里读取文件文本，作为分镜输入
     */
    UPLOAD(2, "上传材料");

    /**
     * 来源
     */
    private final Integer source;
    /**
     * 来源名
     */
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiPptSourceEnum::getSource).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
