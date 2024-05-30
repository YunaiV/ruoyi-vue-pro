package cn.iocoder.yudao.module.ai.client.vo;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Midjourney 提交任务 code 枚举
 *
 * @author fansili
 * @time 2024/5/30 14:33
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum MidjourneySubmitCodeEnum {

    // 状态码: 1(提交成功), 21(已存在), 22(排队中), other(错误)
    SUBMIT_SUCCESS("1", "提交成功"),
    ALREADY_EXISTS("1", "已存在"),
    QUEUING("22", "排队中"),

    ;

    public static final List<String> SUCCESS_CODES = Lists.newArrayList(
            SUBMIT_SUCCESS.code,
            ALREADY_EXISTS.code,
            QUEUING.code
    );

    private String code;
    private String name;

}
