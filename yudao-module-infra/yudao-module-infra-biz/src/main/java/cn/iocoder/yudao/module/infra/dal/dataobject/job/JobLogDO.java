package cn.iocoder.yudao.module.infra.dal.dataobject.job;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.infra.enums.job.JobLogStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 定时任务的执行日志
 *
 * @author 芋道源码
 */
@TableName("infra_job_log")
@KeySequence("infra_job_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobLogDO extends BaseDO {

    /**
     * 日志编号
     */
    private Long id;
    /**
     * 任务编号
     *
     * 关联 {@link JobDO#getId()}
     */
    private Long jobId;
    /**
     * 处理器的名字
     *
     * 冗余字段 {@link JobDO#getHandlerName()}
     */
    private String handlerName;
    /**
     * 处理器的参数
     *
     * 冗余字段 {@link JobDO#getHandlerParam()}
     */
    private String handlerParam;
    /**
     * 第几次执行
     *
     * 用于区分是不是重试执行。如果是重试执行，则 index 大于 1
     */
    private Integer executeIndex;

    /**
     * 开始执行时间
     */
    private LocalDateTime beginTime;
    /**
     * 结束执行时间
     */
    private LocalDateTime endTime;
    /**
     * 执行时长，单位：毫秒
     */
    private Integer duration;
    /**
     * 状态
     *
     * 枚举 {@link JobLogStatusEnum}
     */
    private Integer status;
    /**
     * 结果数据
     *
     * 成功时，使用 {@link JobHandler#execute(String)} 的结果
     * 失败时，使用 {@link JobHandler#execute(String)} 的异常堆栈
     */
    private String result;

}
