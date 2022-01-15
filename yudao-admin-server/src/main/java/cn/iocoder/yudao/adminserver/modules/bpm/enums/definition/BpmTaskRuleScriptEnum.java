package cn.iocoder.yudao.adminserver.modules.bpm.enums.definition;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * BPM 任务规则的脚本枚举
 * 目前暂时通过 TODO 硬编码，未来可以考虑 Groovy 动态脚本的方式
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum BpmTaskRuleScriptEnum {

    ONE(1L, ""),
    TWO(2L, "");

    /**
     * 脚本编号
     */
    private final Long id;
    /**
     * 脚本描述
     */
    private final String desc;

}
