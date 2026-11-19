package cn.iocoder.yudao.module.ai.enums.video;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 视频创作类型的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiVideoTypeEnum implements ArrayValuable<Integer> {

    /**
     * 视频生成：文生、图生、首尾帧、参考，参见 {@link AiVideoModeEnum}
     */
    GENERATE(1, "视频生成"),
    /**
     * 数字人口播：人物图片 + 上传的驱动音频
     */
    AVATAR(2, "数字人口播"),
    /**
     * 动作模仿：人物图片 + 动作视频
     */
    MOTION(3, "动作模仿");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiVideoTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
