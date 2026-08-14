package cn.iocoder.yudao.module.hrm.service.performance.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceAppealReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceConfirmReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceHandleStageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceProcessRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.task.HrmPortalPerformanceTaskPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentStageDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * HRM 绩效流程 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmPerformanceAssessmentProcessService {

    /**
     * 初始化员工绩效考核阶段
     *
     * @param plan 绩效计划
     * @param assessment 员工绩效考核
     */
    void initializeAssessmentStages(
            HrmPerformancePlanDO plan, HrmPerformanceAssessmentDO assessment);

    /**
     * 激活首个绩效评分阶段
     *
     * @param assessment 员工绩效考核
     * @return 激活的绩效考核阶段
     */
    HrmPerformanceAssessmentStageDO activateFirstReviewStage(
            HrmPerformanceAssessmentDO assessment);

    /**
     * 激活指定类型的绩效考核阶段
     *
     * @param assessment 员工绩效考核
     * @param type 阶段类型
     * @return 激活的绩效考核阶段
     */
    HrmPerformanceAssessmentStageDO activateAssessmentStage(
            HrmPerformanceAssessmentDO assessment, Integer type);

    /**
     * 获得当前用户的绩效目标确认任务分页
     *
     * @param userId 用户编号
     * @param reqVO 分页查询
     * @return 绩效考核阶段分页
     */
    PageResult<HrmPerformanceAssessmentStageDO> getMyTargetConfirmationTaskPage(
            Long userId, HrmPortalPerformanceTaskPageReqVO reqVO);

    /**
     * 获得当前用户的绩效结果审核任务分页
     *
     * @param userId 用户编号
     * @param reqVO 分页查询
     * @return 绩效考核阶段分页
     */
    PageResult<HrmPerformanceAssessmentStageDO> getMyResultAuditTaskPage(
            Long userId, HrmPortalPerformanceTaskPageReqVO reqVO);

    /**
     * 获得当前用户的绩效结果确认任务分页
     *
     * @param userId 用户编号
     * @param reqVO 分页查询
     * @return 绩效考核阶段分页
     */
    PageResult<HrmPerformanceAssessmentStageDO> getMyResultConfirmationTaskPage(
            Long userId, HrmPortalPerformanceTaskPageReqVO reqVO);

    /**
     * 获得当前用户的绩效申诉处理任务分页
     *
     * @param userId 用户编号
     * @param reqVO 分页查询
     * @return 绩效考核阶段分页
     */
    PageResult<HrmPerformanceAssessmentStageDO> getMyAppealTaskPage(
            Long userId, HrmPortalPerformanceTaskPageReqVO reqVO);

    /**
     * 获得当前用户各类绩效任务数量
     *
     * @param userId 用户编号
     * @param search 绩效计划名称、员工姓名或工号
     * @return 阶段类型、处理状态与数量的映射
     */
    Map<Integer, Map<Integer, Long>> getMyTaskCountMap(Long userId, String search);

    /**
     * 校验当前用户可查看指定绩效任务阶段
     *
     * @param userId 用户编号
     * @param assessmentId 员工绩效考核编号
     * @param stageId 绩效考核阶段编号
     * @return 绩效考核阶段
     */
    HrmPerformanceAssessmentStageDO validateTaskStage(
            Long userId, Long assessmentId, Long stageId);

    /**
     * 通知待处理绩效阶段的处理人
     *
     * @param assessment 员工绩效考核
     * @param stage 待处理阶段
     */
    void notifyPendingStage(
            HrmPerformanceAssessmentDO assessment, HrmPerformanceAssessmentStageDO stage);

    /**
     * 通知绩效流程处理结果
     *
     * @param assessment 员工绩效考核
     * @param employeeIds 接收通知的员工编号列表
     * @param actionName 处理动作名称
     * @param result 处理结果
     */
    void notifyProcessResult(HrmPerformanceAssessmentDO assessment,
                             Collection<Long> employeeIds, String actionName, String result);

    /**
     * 确认绩效目标
     *
     * @param userId 用户编号
     * @param reqVO 确认信息
     */
    void confirmTarget(Long userId, HrmPortalPerformanceConfirmReqVO reqVO);

    /**
     * 确认绩效结果
     *
     * @param userId 用户编号
     * @param reqVO 确认信息
     */
    void confirmResult(Long userId, HrmPortalPerformanceConfirmReqVO reqVO);

    /**
     * 提交绩效申诉
     *
     * @param userId 用户编号
     * @param reqVO 申诉信息
     * @return 流程处理结果
     */
    HrmPortalPerformanceProcessRespVO submitAppeal(Long userId, HrmPortalPerformanceAppealReqVO reqVO);

    /**
     * 启动绩效结果审核
     *
     * @param assessmentId 员工绩效考核编号
     * @return 流程处理结果
     */
    HrmPortalPerformanceProcessRespVO startResultAudit(Long assessmentId);

    /**
     * 处理绩效结果审核
     *
     * @param userId 用户编号
     * @param reqVO 阶段处理信息
     */
    void handleResultAudit(Long userId, HrmPortalPerformanceHandleStageReqVO reqVO);

    /**
     * 处理绩效申诉
     *
     * @param userId 用户编号
     * @param reqVO 阶段处理信息
     */
    void handleAppeal(Long userId, HrmPortalPerformanceHandleStageReqVO reqVO);

    /**
     * 刷新绩效计划的完成状态
     *
     * @param planId 绩效计划编号
     */
    void refreshPlanCompletionState(Long planId);

    /**
     * 获得超时的绩效申诉阶段列表
     *
     * @param deadlineTime 截止时间
     * @return 绩效申诉阶段列表
     */
    List<HrmPerformanceAssessmentStageDO> getAppealTimeoutStageList(LocalDateTime deadlineTime);

    /**
     * 处理超时的绩效申诉
     *
     * @param stageId 绩效阶段编号
     * @return 是否处理成功
     */
    boolean processAppealTimeout(Long stageId);

}
