package cn.iocoder.dashboard.modules.infra.enums.job;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;

import static org.quartz.impl.jdbcjobstore.Constants.*;

/**
 * 任务状态的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum InfJobStatusEnum {

    /**
     * 初始化中
     */
    INIT(0, Collections.emptySet()),
    /**
     * 开启
     */
    NORMAL(1, Sets.newHashSet(STATE_WAITING, STATE_ACQUIRED, STATE_BLOCKED)),
    /**
     * 暂停
     */
    STOP(2, Sets.newHashSet(STATE_PAUSED, STATE_PAUSED_BLOCKED));

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 对应的 Quartz 触发器的状态集合
     */
    private final Set<String> quartzStates;

}
