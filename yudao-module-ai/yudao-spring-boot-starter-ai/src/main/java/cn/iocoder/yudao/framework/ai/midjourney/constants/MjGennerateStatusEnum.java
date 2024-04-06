package cn.iocoder.yudao.framework.ai.midjourney.constants;

import lombok.Getter;

/**
 * mj 生成状态
 *
 * author: fansili
 * time: 2024/4/6 21:07
 */
@Getter
public enum MjGennerateStatusEnum {


    WAITING("waiting", "等待..."),
    IN_PROGRESS("in_progress", "进行中"),
    COMPLETED("completed", "完成"),

    ;

    MjGennerateStatusEnum(String value, String message) {
        this.value = value;
        this.message = message;
    }

    private String value;

    private String message;
}
