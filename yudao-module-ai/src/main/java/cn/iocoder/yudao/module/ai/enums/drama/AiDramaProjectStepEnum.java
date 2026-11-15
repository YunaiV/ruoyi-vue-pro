package cn.iocoder.yudao.module.ai.enums.drama;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 短剧项目步骤的枚举：保存在 ai_drama_project.status，前端映射为当前步骤
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaProjectStepEnum implements ArrayValuable<Integer> {

    IDEA(1, "故事想法"),
    SCRIPT(2, "剧本审阅"),
    ASSET(3, "资产配置"),
    STORYBOARD(4, "分镜与成片");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiDramaProjectStepEnum::getStep).toArray(Integer[]::new);

    /**
     * 步骤
     */
    private final Integer step;
    /**
     * 步骤名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
