package cn.iocoder.yudao.module.ai.enums.ppt;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI PPT 格式的枚举：只影响分镜提示词里的格式说明，即图中文字的多少
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiPptModeEnum implements ArrayValuable<Integer> {

    /**
     * 详细演示文稿：图中说明文字可适当多一些，适合邮件发送或单独阅读
     */
    DETAILED(1, "详细演示文稿"),
    /**
     * 演示用幻灯片：图中文字尽量精简，为演讲提供支持
     */
    SLIDES(2, "演示用幻灯片");

    /**
     * 格式
     */
    private final Integer mode;
    /**
     * 格式名
     */
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiPptModeEnum::getMode).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
