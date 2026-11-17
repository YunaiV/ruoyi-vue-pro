package cn.iocoder.yudao.module.ai.enums.drama;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * AI 短剧生成任务类型的枚举：管理端「生成任务」把 5 类记录上的状态汇总为一张视图
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaTaskTypeEnum implements ArrayValuable<Integer> {

    APPEARANCE(1, "角色形象"),
    LOCATION(2, "场景图"),
    VISUAL_ASSET(3, "视觉资产"),
    STORYBOARD(4, "镜头视频"),
    COMPOSE(5, "成片合成");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiDramaTaskTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static AiDramaTaskTypeEnum valueOfType(Integer type) {
        return ArrayUtil.firstMatch(o -> Objects.equals(o.getType(), type), values());
    }

}
