package cn.iocoder.yudao.module.system.service.logger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.operatelog.core.service.OperateLogFrameworkService;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.operatelog.OperateLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.logger.OperateLogDO;

import java.util.List;

/**
 * 操作日志 Service 接口
 */
public interface OperateLogService extends OperateLogFrameworkService {

    /**
     * 获得操作日志分页列表
     *
     * @param reqVO 分页条件
     * @return 操作日志分页列表
     */
    PageResult<OperateLogDO> getOperateLogPage(OperateLogPageReqVO reqVO);

    /**
     * 获得操作日志列表
     *
     * @param reqVO 列表条件
     * @return 日志列表
     */
    List<OperateLogDO> getOperateLogs(OperateLogExportReqVO reqVO);

}
