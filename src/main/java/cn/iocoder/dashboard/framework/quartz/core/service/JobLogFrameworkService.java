package cn.iocoder.dashboard.framework.quartz.core.service;

import java.util.Date;

/**
 * Job 日志 Framework Service 接口
 *
 * @author 芋道源码
 */
public interface JobLogFrameworkService {

    /**
     * 创建 Job 日志
     *
     * @param jobId 任务编号
     * @param jobHandlerName Job 处理器的名字
     * @param jobHandlerParam Job 处理器的参数
     * @return Job 日志的编号
     */
    Long createJobLog(Long jobId, String jobHandlerName, String jobHandlerParam);

    /**
     * 更新 Job 日志成功
     *
     * @param id 日志编号
     * @param endTime 结束时间。因为是异步，避免记录时间不准去
     * @param duration 运行时长，单位：毫秒
     * @param data 成功数据
     */
    void updateJobLogSuccessAsync(Long id, Date endTime, Long duration, String data);

    /**
     * 更新 Job 日志失败
     *
     * @param id 日志编号
     * @param endTime 结束时间。因为是异步，避免记录时间不准去
     * @param duration 运行时长，单位：毫秒
     * @param code 错误码
     * @param msg 异常提示
     */
    void updateJobLogErrorAsync(Long id, Date endTime, Long duration, Integer code, String msg);

}
