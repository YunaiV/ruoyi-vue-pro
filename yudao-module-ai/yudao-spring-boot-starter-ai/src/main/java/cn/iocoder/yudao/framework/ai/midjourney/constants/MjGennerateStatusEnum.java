package cn.iocoder.yudao.framework.ai.midjourney.constants;

import lombok.Getter;

// TODO @fansili：1）Mj 缩写，还是搞成全称。。虽然长一点，但是感觉会相对清晰一些哈；2）lombok 相关的注解，可以用用哈；3）value 改 status；
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
