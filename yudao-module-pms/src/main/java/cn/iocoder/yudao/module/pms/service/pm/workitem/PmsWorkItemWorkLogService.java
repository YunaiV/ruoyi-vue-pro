package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsProjectWorkLogReportReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsProjectWorkLogReportRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsWorkItemWorkLogSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog.PmsWorkItemWorkLogSummaryRespVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemWorkLogDO;

import java.util.Collection;
import java.util.List;

/**
 * PMS 工作项工时记录 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsWorkItemWorkLogService {

    /**
     * 创建工时记录
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     * @return 工时记录编号
     */
    Long createWorkItemWorkLog(PmsWorkItemWorkLogSaveReqVO saveReqVO, Long userId);

    /**
     * 更新工时记录
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     */
    void updateWorkItemWorkLog(PmsWorkItemWorkLogSaveReqVO saveReqVO, Long userId);

    /**
     * 获得工时记录
     *
     * @param id 工时记录编号
     * @param userId 用户编号
     * @return 工时记录
     */
    PmsWorkItemWorkLogDO getWorkItemWorkLog(Long id, Long userId);

    /**
     * 获得工作项工时汇总
     *
     * @param workItemId 工作项编号
     * @param userId 用户编号
     * @return 工时汇总
     */
    PmsWorkItemWorkLogSummaryRespVO getWorkItemWorkLogSummary(Long workItemId, Long userId);

    /**
     * 获得多个工作项的工时记录，供迭代燃尽统计使用
     *
     * @param workItemIds 工作项编号集合
     * @return 工时记录
     */
    List<PmsWorkItemWorkLogDO> getWorkItemWorkLogListByWorkItemIds(Collection<Long> workItemIds);

    /**
     * 获得项目工时报表，按迭代和工作项汇总日期范围内的每日工时
     *
     * @param reqVO 报表查询条件
     * @param userId 用户编号
     * @return 工时报表
     */
    PmsProjectWorkLogReportRespVO getProjectWorkItemWorkLogReport(PmsProjectWorkLogReportReqVO reqVO, Long userId);

    /**
     * 删除项目的全部工时记录
     *
     * @param projectId 项目编号
     */
    void deleteWorkItemWorkLogListByProjectId(Long projectId);

    /**
     * 删除工作项的全部工时，用于工作项删除时级联清理
     *
     * @param workItemId 工作项编号
     */
    void deleteWorkItemWorkLogListByWorkItemId(Long workItemId);

}
