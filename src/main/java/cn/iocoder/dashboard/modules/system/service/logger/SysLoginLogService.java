package cn.iocoder.dashboard.modules.system.service.logger;

import cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog.SysLoginLogCreateReqVO;

/**
 * 登陆日志 Service 接口
 */
public interface SysLoginLogService {

    /**
     * 创建登陆日志
     *
     * @param reqVO 日志信息
     */
    void createLoginLog(SysLoginLogCreateReqVO reqVO);

}
