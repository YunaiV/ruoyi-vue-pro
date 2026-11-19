package cn.iocoder.yudao.module.ai.enums.ppt;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI PPT 状态的枚举
 *
 * 1. 任务：分镜、配图、继续生成、单页重绘都是生成中，阶段由分镜页数、每页状态区分
 * 2. 每页（slides 里的 status）：同样使用这三个值，为空表示待生成
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiPptStatusEnum implements ArrayValuable<Integer> {

    IN_PROGRESS(10, "生成中"),
    SUCCESS(20, "已完成"),
    FAIL(30, "已失败");

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiPptStatusEnum::getStatus).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
