package cn.iocoder.dashboard.modules.system.service.logger;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.logger.apilog.core.service.ApiAccessLogFrameworkService;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.apiaccesslog.SysApiAccessLogExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.apiaccesslog.SysApiAccessLogPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.logger.SysApiAccessLogDO;

import java.util.List;

/**
 * API 访问日志 Service 接口
 *
 * @author 芋道源码
 */
public interface SysApiAccessLogService extends ApiAccessLogFrameworkService {

    /**
     * 获得 API 访问日志
     *
     * @param id 编号
     * @return API 访问日志
     */
    SysApiAccessLogDO getApiAccessLog(Long id);

    /**
     * 获得 API 访问日志分页
     *
     * @param pageReqVO 分页查询
     * @return API 访问日志分页
     */
    PageResult<SysApiAccessLogDO> getApiAccessLogPage(SysApiAccessLogPageReqVO pageReqVO);

    /**
     * 获得 API 访问日志列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return API 访问日志分页
     */
    List<SysApiAccessLogDO> getApiAccessLogList(SysApiAccessLogExportReqVO exportReqVO);

}
