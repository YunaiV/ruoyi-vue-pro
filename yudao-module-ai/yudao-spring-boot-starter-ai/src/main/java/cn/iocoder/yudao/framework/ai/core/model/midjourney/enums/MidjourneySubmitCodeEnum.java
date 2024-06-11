package cn.iocoder.yudao.framework.ai.core.model.midjourney.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// TODO @fan：待定
/**
 * Midjourney 提交任务 code 枚举
 *
 * @author fansili
 */
@Getter
@AllArgsConstructor
public enum MidjourneySubmitCodeEnum {

    SUBMIT_SUCCESS("1", "提交成功"),
    ALREADY_EXISTS("21", "已存在"),
    QUEUING("22", "排队中"),
    ;

    public static final List<String> SUCCESS_CODES = Lists.newArrayList(
            SUBMIT_SUCCESS.code,
            ALREADY_EXISTS.code,
            QUEUING.code
    );

    private final String code;
    private final String name;

}
