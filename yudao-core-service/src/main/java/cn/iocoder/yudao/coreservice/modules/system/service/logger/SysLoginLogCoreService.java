package cn.iocoder.yudao.coreservice.modules.system.service.logger;

import cn.iocoder.yudao.coreservice.modules.system.service.logger.dto.SysLoginLogCreateReqDTO;

/**
 * 登录日志 Core Service 接口
 */
public interface SysLoginLogCoreService {

    /**
     * 创建登录日志
     *
     * @param reqDTO 日志信息
     */
    void createLoginLog(SysLoginLogCreateReqDTO reqDTO);

}
