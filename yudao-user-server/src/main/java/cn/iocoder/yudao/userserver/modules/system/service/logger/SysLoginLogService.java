package cn.iocoder.yudao.userserver.modules.system.service.logger;

import cn.iocoder.yudao.userserver.modules.system.service.logger.dto.SysLoginLogCreateReqDTO;

/**
 * 登录日志 Service 接口
 */
public interface SysLoginLogService {

    /**
     * 创建登录日志
     *
     * @param reqDTO 日志信息
     */
    void createLoginLog(SysLoginLogCreateReqDTO reqDTO);

}
