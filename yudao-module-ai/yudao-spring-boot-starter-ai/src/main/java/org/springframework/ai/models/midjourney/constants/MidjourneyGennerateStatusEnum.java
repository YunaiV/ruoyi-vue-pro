package org.springframework.ai.models.midjourney.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

// TODO done @fansili：1）Mj 缩写，还是搞成全称。。虽然长一点，但是感觉会相对清晰一些哈；2）lombok 相关的注解，可以用用哈；3）value 改 status；
/**
 * mj 生成状态
 *
 * author: fansili
 * time: 2024/4/6 21:07
 */
@Getter
@AllArgsConstructor
public enum MidjourneyGennerateStatusEnum {

    WAITING("waiting", "等待..."),
    IN_PROGRESS("in_progress", "进行中"),
    COMPLETED("completed", "完成"),

    ;

    /**
     * 状态
     */
    private String status;
    /**
     * 状态信息
     */
    private String message;
}
