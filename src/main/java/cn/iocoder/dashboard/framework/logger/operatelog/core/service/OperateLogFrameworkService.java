package cn.iocoder.dashboard.framework.logger.operatelog.core.service;

import cn.iocoder.dashboard.modules.system.controller.logger.vo.operatelog.SysOperateLogCreateReqVO;

public interface OperateLogFrameworkService {

    /**
     * 要不记录操作日志
     *
     * @param reqVO 操作日志请求
     */
    void createOperateLogAsync(SysOperateLogCreateReqVO reqVO);

}
