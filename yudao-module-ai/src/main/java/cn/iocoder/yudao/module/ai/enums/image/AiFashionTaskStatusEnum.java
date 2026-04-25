package cn.iocoder.yudao.module.ai.enums.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 服装设计流水线任务状态枚举
 *
 * @author deepay
 */
@AllArgsConstructor
@Getter
public enum AiFashionTaskStatusEnum {

    IN_PROGRESS(10, "进行中"),
    SUCCESS(20, "已完成"),
    FAIL(30, "已失败");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名称
     */
    private final String name;

}
