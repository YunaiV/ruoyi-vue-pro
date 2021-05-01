package cn.iocoder.dashboard.modules.infra.service.logger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.apilog.core.service.ApiAccessLogFrameworkService;
import cn.iocoder.dashboard.modules.infra.controller.logger.vo.apiaccesslog.InfApiAccessLogExportReqVO;
import cn.iocoder.dashboard.modules.infra.controller.logger.vo.apiaccesslog.InfApiAccessLogPageReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.logger.InfApiAccessLogDO;

import java.util.List;

/**
 * API 访问日志 Service 接口
 *
 * @author 芋道源码
 */
public interface InfApiAccessLogService extends ApiAccessLogFrameworkService {

    /**
     * 获得 API 访问日志分页
     *
     * @param pageReqVO 分页查询
     * @return API 访问日志分页
     */
    PageResult<InfApiAccessLogDO> getApiAccessLogPage(InfApiAccessLogPageReqVO pageReqVO);

    /**
     * 获得 API 访问日志列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return API 访问日志分页
     */
    List<InfApiAccessLogDO> getApiAccessLogList(InfApiAccessLogExportReqVO exportReqVO);

}
