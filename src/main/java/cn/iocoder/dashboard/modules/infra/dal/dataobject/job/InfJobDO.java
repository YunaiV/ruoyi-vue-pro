package cn.iocoder.dashboard.modules.infra.dal.dataobject.job;

import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.dashboard.modules.infra.enums.job.InfJobStatusEnum;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.Date;

/**
 * 定时任务 DO
 *
 * @author 芋道源码
 */
@TableName("inf_job")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfJobDO extends BaseDO {

    /**
     * 任务编号
     */
    @TableId
    private Long id;
    /**
     * 任务名称
     */
    private String name;
    /**
     * 任务状态
     *
     * 枚举 {@link InfJobStatusEnum}
     */
    private Integer status;
    /**
     * 处理器的名字
     */
    private String handlerName;
    /**
     * 处理器的参数
     */
    private String handlerParam;

    // ========== 时间相关字段 ==========

    /**
     * CRON 表达式
     */
    private String cronExpression;
    /**
     * 最后一次执行的开始时间
     *
     * 该字段在任务执行结束后，进行记录
     */
    private Date executeBeginTime;
    /**
     * 最后一次执行的结束时间
     *
     * 该字段在任务执行结束后，进行记录
     */
    private Date executeEndTime;
    /**
     * 上一次触发时间，来自 Quartz SCHE_TRIGGERS 表
     */
    private Date firePrevTime;
    /**
     * 下一次触发时间，来自 Quartz SCHE_TRIGGERS 表
     *
     * 在触发器状态从 `ACQUIRED` 变成 `BLOCKED` 时，就会更新 PREV_FIRE_TIME、NEXT_FIRE_TIME，然后定时任务才正式开始执行
     */
    private Date fireNextTime;

    // ========== 重试相关字段 ==========
    /**
     * 重试次数
     * 如果不重试，则设置为 0
     */
    private Integer retryCount;
    /**
     * 重试间隔，单位：毫秒
     * 如果没有间隔，则设置为 0
     */
    private Integer retryInterval;

    // ========== 监控相关字段 ==========
    /**
     * 监控超时时间，单位：毫秒
     * 为空时，表示不监控
     *
     * 注意，这里的超时的目的，不是进行任务的取消，而是告警任务的执行时间过长
     */
    private Integer monitorTimeout;

    // TODO misfirePolicy
    // TODO concurrent
    // TODO 失败重试

}
