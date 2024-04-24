package cn.iocoder.yudao.module.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ai modal disable
 *
 * @author fansili
 * @time 2024/4/24 20:15
 * @since 1.0
 */
@AllArgsConstructor
@Getter
public enum AiChatModalDisableEnum {

    NO(0, "未禁用"),
    YES(1, "禁用"),


    ;

    private Integer value;

    private String name;
}
