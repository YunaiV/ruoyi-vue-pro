package cn.iocoder.dashboard.modules.system.service.logger;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.logger.operatelog.core.service.OperateLogFrameworkService;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.SysOperateLogPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.logger.SysOperateLogDO;

/**
 * 操作日志 Service 接口
 */
public interface SysOperateLogService extends OperateLogFrameworkService {

    /**
     * 获得操作日志分页列表
     *
     * @param reqVO 分页条件
     * @return 操作日志分页列表
     */
    PageResult<SysOperateLogDO> pageOperateLog(SysOperateLogPageReqVO reqVO);

}
