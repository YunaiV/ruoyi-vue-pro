package com.ruoyi.project.monitor.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * 定时任务调度日志表 sys_job_log
 * 
 * @author ruoyi
 */
public class SysJobLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    @Excel(name = "日志序号")
    private Long jobLogId;

    /** 任务名称 */
    @Excel(name = "任务名称")
    private String jobName;

    /** 任务组名 */
    @Excel(name = "任务组名")
    private String jobGroup;

    /** 调用目标字符串 */
    @Excel(name = "调用目标字符串")
    private String invokeTarget;

    /** 日志信息 */
    @Excel(name = "日志信息")
    private String jobMessage;

    /** 执行状态（0正常 1失败） */
    @Excel(name = "执行状态", readConverterExp = "0=正常,1=失败")
    private String status;

    /** 异常信息 */
    @Excel(name = "异常信息")
    private String exceptionInfo;

    /** 开始时间 */
    private Date startTime;

    /** 停止时间 */
    private Date stopTime;

    public Long getJobLogId()
    {
        return jobLogId;
    }

    public void setJobLogId(Long jobLogId)
    {
        this.jobLogId = jobLogId;
    }

    public String getJobName()
    {
        return jobName;
    }

    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    public String getJobGroup()
    {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup)
    {
        this.jobGroup = jobGroup;
    }

    public String getInvokeTarget()
    {
        return invokeTarget;
    }

    public void setInvokeTarget(String invokeTarget)
    {
        this.invokeTarget = invokeTarget;
    }

    public String getJobMessage()
    {
        return jobMessage;
    }

    public void setJobMessage(String jobMessage)
    {
        this.jobMessage = jobMessage;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getExceptionInfo()
    {
        return exceptionInfo;
    }

    public void setExceptionInfo(String exceptionInfo)
    {
        this.exceptionInfo = exceptionInfo;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }
    
    public Date getStopTime()
    {
        return stopTime;
    }

    public void setStopTime(Date stopTime)
    {
        this.stopTime = stopTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("jobLogId", getJobLogId())
            .append("jobName", getJobName())
            .append("jobGroup", getJobGroup())
            .append("jobMessage", getJobMessage())
            .append("status", getStatus())
            .append("exceptionInfo", getExceptionInfo())
            .append("startTime", getStartTime())
            .append("stopTime", getStopTime())
            .toString();
    }
}
