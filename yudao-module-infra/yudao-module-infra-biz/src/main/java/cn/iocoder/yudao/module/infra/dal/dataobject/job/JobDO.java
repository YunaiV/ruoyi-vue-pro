package cn.iocoder.yudao.module.infra.dal.dataobject.job;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.infra.enums.job.JobStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 定时任务 DO
 *
 * @author 芋道源码
 */
@TableName("infra_job")
@KeySequence("infra_job_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDO extends BaseDO {

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
     * 枚举 {@link JobStatusEnum}
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
    /**
     * CRON 表达式
     */
    private String cronExpression;

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

}
