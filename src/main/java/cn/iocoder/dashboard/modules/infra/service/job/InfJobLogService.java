package cn.iocoder.dashboard.modules.infra.service.job;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.quartz.core.service.JobLogFrameworkService;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.log.InfJobLogExportReqVO;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.log.InfJobLogPageReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.job.InfJobLogDO;

import java.util.Collection;
import java.util.List;

/**
 * Job 日志 Service 接口
 *
 * @author 芋道源码
 */
public interface InfJobLogService extends JobLogFrameworkService {

    /**
     * 获得定时任务
     *
     * @param id 编号
     * @return 定时任务
     */
    InfJobLogDO getJobLog(Long id);

    /**
     * 获得定时任务列表
     *
     * @param ids 编号
     * @return 定时任务列表
     */
    List<InfJobLogDO> getJobLogList(Collection<Long> ids);

    /**
     * 获得定时任务分页
     *
     * @param pageReqVO 分页查询
     * @return 定时任务分页
     */
    PageResult<InfJobLogDO> getJobLogPage(InfJobLogPageReqVO pageReqVO);

    /**
     * 获得定时任务列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 定时任务分页
     */
    List<InfJobLogDO> getJobLogList(InfJobLogExportReqVO exportReqVO);

}
