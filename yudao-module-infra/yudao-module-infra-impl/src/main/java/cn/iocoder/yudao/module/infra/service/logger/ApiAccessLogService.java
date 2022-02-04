package cn.iocoder.yudao.module.infra.service.logger;

import cn.iocoder.yudao.framework.apilog.core.service.ApiAccessLogFrameworkService;
import cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogPageReqVO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.dal.dataobject.logger.ApiAccessLogDO;

import java.util.List;

/**
 * API 访问日志 Service 接口
 *
 * @author 芋道源码
 */
public interface ApiAccessLogService extends ApiAccessLogFrameworkService {

    /**
     * 获得 API 访问日志分页
     *
     * @param pageReqVO 分页查询
     * @return API 访问日志分页
     */
    PageResult<ApiAccessLogDO> getApiAccessLogPage(ApiAccessLogPageReqVO pageReqVO);

    /**
     * 获得 API 访问日志列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return API 访问日志分页
     */
    List<ApiAccessLogDO> getApiAccessLogList(ApiAccessLogExportReqVO exportReqVO);

}
