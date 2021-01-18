package cn.iocoder.dashboard.modules.system.service.logger;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog.SysLoginLogCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog.SysLoginLogPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.logger.SysLoginLogDO;

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

    /**
     * 获得登陆日志分页
     *
     * @param reqVO 分页条件
     * @return 登陆日志分页
     */
    PageResult<SysLoginLogDO> getLoginLogPage(SysLoginLogPageReqVO reqVO);

}
