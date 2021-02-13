package cn.iocoder.dashboard.modules.infra.dal.dataobject.job;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * 定时任务的执行日志 sys_job_log
 *
 * @author 芋道源码
 */
@TableName("inf_job_log")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InfJobLog extends BaseDO {

    /**
     * 日志编号
     */
    private Long id;
    /**
     * 任务编号
     *
     * 关联 {@link InfJob#getId()}
     */
    private Long jobId;
    /**
     * 任务名称
     *
     * 冗余字段 {@link InfJob#getName()}
     */
    private String jobName;
    /**
     * 任务分组
     *
     * 冗余字段 {@link InfJob#getGroup()}
     */
    private String jobGroup;
    /**
     * 处理器的名字
     *
     * 冗余字段 {@link InfJob#getHandlerName()}
     */
    private String handlerName;
    /**
     * 处理器的参数
     *
     * 冗余字段 {@link InfJob#getHandlerParam()}
     */
    private String handlerParam;

    /**
     * 开始执行时间
     */
    private Date startTime;
    /**
     * 结束执行时间
     */
    private Date endTime;
    /**
     * 执行时长，单位：毫秒
     */
    private Integer duration;
    /**
     * 结果码
     *
     * 目前使用的 {@link CommonResult#getCode()} 属性
     */
    private Integer resultCode;
    /**
     * 结果提示
     *
     * 目前使用的 {@link CommonResult#getMsg()} 属性
     */
    private String resultMsg;
    /**
     * 结果数据
     *
     * 如果是对象，则使用 JSON 格式化
     */
    private String resultData;

}
