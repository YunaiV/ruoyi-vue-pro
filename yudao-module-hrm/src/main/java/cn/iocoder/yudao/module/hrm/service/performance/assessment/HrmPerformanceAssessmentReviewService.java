package cn.iocoder.yudao.module.hrm.service.performance.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceFillQuotaReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceProcessRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceReviewRejectReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceScorePreviewRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceScoreReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.task.HrmPortalPerformanceTaskPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentStageDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;

/**
 * HRM 绩效评分 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmPerformanceAssessmentReviewService {

    /**
     * 获得当前用户的绩效指标填写任务分页
     *
     * @param userId 用户编号
     * @param reqVO 分页查询
     * @return 员工绩效考核阶段分页
     */
    PageResult<HrmPerformanceAssessmentStageDO> getMyFillQuotaTaskPage(
            Long userId, HrmPortalPerformanceTaskPageReqVO reqVO);

    /**
     * 获得当前用户的绩效评分任务分页
     *
     * @param userId 用户编号
     * @param reqVO 分页查询
     * @return 员工绩效考核阶段分页
     */
    PageResult<HrmPerformanceAssessmentStageDO> getMyReviewTaskPage(
            Long userId, HrmPortalPerformanceTaskPageReqVO reqVO);

    /**
     * 确保员工绩效考核已初始化指标
     *
     * @param plan 绩效计划
     * @param assessment 员工绩效考核
     */
    void ensureAssessmentQuotaList(
            HrmPerformancePlanDO plan, HrmPerformanceAssessmentDO assessment);

    /**
     * 填写绩效指标
     *
     * @param userId 用户编号
     * @param reqVO 指标信息
     */
    void fillQuota(Long userId, HrmPortalPerformanceFillQuotaReqVO reqVO);

    /**
     * 预览绩效评分结果
     *
     * @param userId 用户编号
     * @param reqVO 评分信息
     * @return 评分试算结果
     */
    HrmPortalPerformanceScorePreviewRespVO previewScore(Long userId, HrmPortalPerformanceScoreReqVO reqVO);

    /**
     * 提交绩效评分
     *
     * @param userId 用户编号
     * @param reqVO 评分信息
     * @return 流程处理结果
     */
    HrmPortalPerformanceProcessRespVO scoreAssessment(Long userId, HrmPortalPerformanceScoreReqVO reqVO);

    /**
     * 驳回绩效评分阶段
     *
     * @param userId 用户编号
     * @param reqVO 驳回信息
     */
    void rejectReviewStage(Long userId, HrmPortalPerformanceReviewRejectReqVO reqVO);

}
