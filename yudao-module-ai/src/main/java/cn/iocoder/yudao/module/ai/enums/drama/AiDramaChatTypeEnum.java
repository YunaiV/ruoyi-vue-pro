package cn.iocoder.yudao.module.ai.enums.drama;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * AI 短剧对话模型生成类型的枚举：生成剧本、打磨剧本、分析资产、拆分分镜，以及为已有分镜生成摄影规则、表演指导、关键帧规划
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaChatTypeEnum implements ArrayValuable<Integer> {

    SCRIPT_GENERATE(1, "生成剧本", true),
    SCRIPT_POLISH(2, "打磨剧本", true),
    ASSET_ANALYZE(3, "分析资产", true),
    STORYBOARD_GENERATE(4, "拆分分镜", false),
    PHOTOGRAPHY_RULES(5, "生成摄影规则", false),
    ACTING_NOTES(6, "生成表演指导", false),
    FRAME_PLAN(7, "规划关键帧", false);

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiDramaChatTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名：用于提示
     */
    private final String name;
    /**
     * 是否要求模型返回 JSON 对象：拆分分镜、摄影规则、表演指导、关键帧规划返回 JSON 数组，为 false
     */
    private final boolean jsonObject;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static AiDramaChatTypeEnum valueOfType(Integer type) {
        return ArrayUtil.firstMatch(o -> Objects.equals(o.getType(), type), values());
    }

}
