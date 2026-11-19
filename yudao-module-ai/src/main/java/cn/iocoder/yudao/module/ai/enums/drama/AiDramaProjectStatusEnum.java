package cn.iocoder.yudao.module.ai.enums.drama;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 短剧项目状态的枚举：项目的生命周期，例如剧本变更后，已有的资产、分镜需要更新
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaProjectStatusEnum implements ArrayValuable<Integer> {

    DRAFT(10, "草稿"),
    SCRIPT_CHANGED(20, "剧本已变更"),
    STORYBOARD_READY(30, "分镜就绪");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiDramaProjectStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
