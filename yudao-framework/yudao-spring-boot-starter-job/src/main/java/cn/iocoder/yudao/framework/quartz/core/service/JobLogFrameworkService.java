package cn.iocoder.yudao.framework.quartz.core.service;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
     * @param jobId           任务编号
     * @param beginTime       开始时间
     * @param jobHandlerName  Job 处理器的名字
     * @param jobHandlerParam Job 处理器的参数
     * @param executeIndex    第几次执行
     * @return Job 日志的编号
     */
    Long createJobLog(@NotNull(message = "任务编号不能为空") Long jobId,
                      @NotNull(message = "开始时间") Date beginTime,
                      @NotEmpty(message = "Job 处理器的名字不能为空") String jobHandlerName,
                      String jobHandlerParam,
                      @NotNull(message = "第几次执行不能为空") Integer executeIndex);

    /**
     * 更新 Job 日志的执行结果
     *
     * @param logId    日志编号
     * @param endTime  结束时间。因为是异步，避免记录时间不准去
     * @param duration 运行时长，单位：毫秒
     * @param success  是否成功
     * @param result   成功数据
     */
    void updateJobLogResultAsync(@NotNull(message = "日志编号不能为空") Long logId,
                                 @NotNull(message = "结束时间不能为空") Date endTime,
                                 @NotNull(message = "运行时长不能为空") Integer duration,
                                 boolean success, String result);

}
