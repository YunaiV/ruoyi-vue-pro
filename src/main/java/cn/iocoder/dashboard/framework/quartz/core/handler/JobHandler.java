package cn.iocoder.dashboard.framework.quartz.core.handler;

import cn.iocoder.dashboard.common.pojo.CommonResult;

/**
 * 任务处理器
 *
 * @author 芋道源码
 */
public interface JobHandler {

    /**
     * 执行任务
     *
     * @param param 参数
     * @return 结果
     * @throws Exception 异常
     */
    CommonResult<String> execute(String param) throws Exception;

}
