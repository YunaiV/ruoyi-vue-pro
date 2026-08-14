package cn.iocoder.yudao.module.hrm.service.performance.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceArchiveEmployeePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceArchivePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.assessment.HrmPortalPerformanceAssessmentPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentAppealRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDimensionDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentQuotaDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentQuotaScoreDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentStageDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * HRM 员工绩效考核 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmPerformanceAssessmentService {

    /**
     * 获得员工绩效考核
     *
     * @param id 员工绩效考核编号
     * @return 员工绩效考核
     */
    HrmPerformanceAssessmentDO getPerformanceAssessment(Long id);

    /**
     * 获得员工绩效考核列表
     *
     * @param ids 员工绩效考核编号集合
     * @return 员工绩效考核列表
     */
    List<HrmPerformanceAssessmentDO> getPerformanceAssessmentList(Collection<Long> ids);

    /**
     * 获得员工绩效考核 Map
     *
     * @param ids 员工绩效考核编号集合
     * @return 员工绩效考核 Map
     */
    default Map<Long, HrmPerformanceAssessmentDO> getPerformanceAssessmentMap(Collection<Long> ids) {
        return convertMap(getPerformanceAssessmentList(ids), HrmPerformanceAssessmentDO::getId);
    }

    /**
     * 获得绩效计划的员工考核列表
     *
     * @param planId 绩效计划编号
     * @return 员工考核列表
     */
    List<HrmPerformanceAssessmentDO> getPerformanceAssessmentListByPlanId(Long planId);

    /**
     * 获得多个绩效计划的员工考核列表
     *
     * @param planIds 绩效计划编号集合
     * @return 员工考核列表
     */
    List<HrmPerformanceAssessmentDO> getPerformanceAssessmentListByPlanIds(Collection<Long> planIds);

    /**
     * 校验员工绩效考核是否存在
     *
     * @param id 员工绩效考核编号
     * @return 员工绩效考核
     */
    HrmPerformanceAssessmentDO validatePerformanceAssessmentExists(Long id);

    /**
     * 校验参评记录属于指定员工
     *
     * @param id 参评记录编号
     * @param employeeId 员工编号
     * @return 参评记录
     */
    HrmPerformanceAssessmentDO validatePerformanceAssessmentByEmployeeId(Long id, Long employeeId);

    /**
     * 获得员工绩效考核分页
     *
     * @param reqVO 分页查询
     * @return 员工绩效考核分页
     */
    PageResult<HrmPerformanceAssessmentDO> getPerformanceAssessmentPage(
            HrmPerformanceAssessmentPageReqVO reqVO);

    /**
     * 获得员工自己的绩效考核分页
     *
     * @param reqVO 分页查询
     * @param employeeId 员工编号
     * @return 员工绩效考核分页
     */
    PageResult<HrmPerformanceAssessmentDO> getPortalPerformanceAssessmentPage(
            HrmPortalPerformanceAssessmentPageReqVO reqVO, Long employeeId);

    /**
     * 获得员工自己的绩效考核状态数量
     *
     * @param employeeId 员工编号
     * @param planName 绩效计划名称
     * @return 考核状态与数量的映射
     */
    Map<Integer, Long> getPerformanceAssessmentStatusCountMapByEmployeeId(
            Long employeeId, String planName);

    /**
     * 获得绩效归档分页
     *
     * @param reqVO 分页查询
     * @return 绩效归档分页
     */
    PageResult<HrmPerformanceAssessmentDO> getPerformanceAssessmentArchivePage(
            HrmPerformanceArchivePageReqVO reqVO);

    /**
     * 获得存在归档绩效的员工分页
     *
     * @param reqVO 分页查询
     * @return 员工分页
     */
    PageResult<HrmEmployeeDO> getPerformanceArchiveEmployeePage(
            HrmPerformanceArchiveEmployeePageReqVO reqVO);

    /**
     * 获得绩效归档记录
     *
     * @param id 员工绩效考核编号
     * @return 绩效归档记录
     */
    HrmPerformanceAssessmentDO getPerformanceAssessmentArchive(Long id);

    /**
     * 获得指定员工和状态的绩效考核列表
     *
     * @param employeeIds 员工编号集合
     * @param status 考核状态
     * @return 绩效考核列表
     */
    List<HrmPerformanceAssessmentDO> getPerformanceAssessmentListByEmployeeIdsAndStatus(
            Collection<Long> employeeIds, Integer status);

    /**
     * 获得绩效考核维度列表
     *
     * @param assessmentIds 员工绩效考核编号集合
     * @return 绩效考核维度列表
     */
    List<HrmPerformanceAssessmentDimensionDO> getPerformanceAssessmentDimensionList(
            Collection<Long> assessmentIds);

    /**
     * 获得绩效考核指标列表
     *
     * @param assessmentIds 员工绩效考核编号集合
     * @return 绩效考核指标列表
     */
    List<HrmPerformanceAssessmentQuotaDO> getPerformanceAssessmentQuotaList(
            Collection<Long> assessmentIds);

    /**
     * 获得绩效考核阶段列表
     *
     * @param assessmentIds 员工绩效考核编号集合
     * @return 绩效考核阶段列表
     */
    List<HrmPerformanceAssessmentStageDO> getPerformanceAssessmentStageList(
            Collection<Long> assessmentIds);

    /**
     * 获得绩效考核指标评分列表
     *
     * @param assessmentStageIds 员工绩效考核阶段编号集合
     * @return 绩效考核指标评分列表
     */
    List<HrmPerformanceAssessmentQuotaScoreDO> getPerformanceAssessmentQuotaScoreList(
            Collection<Long> assessmentStageIds);

    /**
     * 获得绩效考核申诉记录列表
     *
     * @param assessmentIds 员工绩效考核编号集合
     * @param status 处理状态
     * @return 绩效考核申诉记录列表
     */
    List<HrmPerformanceAssessmentAppealRecordDO> getPerformanceAssessmentAppealRecordList(
            Collection<Long> assessmentIds, Integer status);

    /**
     * 获得指定状态的员工绩效考核列表
     *
     * @param status 考核状态
     * @return 员工绩效考核列表
     */
    List<HrmPerformanceAssessmentDO> getPerformanceAssessmentListByStatus(Integer status);

    /**
     * 删除绩效归档记录
     *
     * @param ids 员工绩效考核编号集合
     */
    void deletePerformanceArchiveRecords(Collection<Long> ids);

    /**
     * 删除员工的全部绩效归档记录
     *
     * @param employeeIds 员工编号集合
     */
    void deletePerformanceArchiveRecordsByEmployeeIds(Collection<Long> employeeIds);

    /**
     * 获得指定计划和员工的已归档绩效系数
     *
     * @param planIds 绩效计划编号集合
     * @param employeeIds 员工编号集合
     * @return 员工编号与绩效系数的映射
     */
    Map<Long, BigDecimal> getPerformanceArchiveEmployeeCoefficientMap(
            Collection<Long> planIds, Collection<Long> employeeIds);

    /**
     * 添加员工绩效考核
     *
     * @param plan 绩效计划
     * @param employeeIds 员工编号集合
     */
    void addPerformanceAssessmentList(HrmPerformancePlanDO plan, Collection<Long> employeeIds);

    /**
     * 同步员工绩效考核
     *
     * @param plan 绩效计划
     * @param employeeIds 员工编号集合
     */
    void syncPerformanceAssessmentList(HrmPerformancePlanDO plan, Collection<Long> employeeIds);

    /**
     * 删除指定员工的绩效考核
     *
     * @param planId 绩效计划编号
     * @param employeeIds 员工编号集合
     */
    void deletePerformanceAssessmentList(Long planId, Collection<Long> employeeIds);

    /**
     * 删除绩效计划的全部员工考核
     *
     * @param planId 绩效计划编号
     */
    void deletePerformanceAssessmentListByPlanId(Long planId);

    /**
     * 终止绩效计划的全部员工考核
     *
     * @param planId 绩效计划编号
     * @param operatorEmployeeId 操作员工编号；未绑定员工时为空
     */
    void terminatePerformanceAssessmentListByPlanId(Long planId, Long operatorEmployeeId);

    /**
     * 启动绩效计划下的员工考核
     *
     * @param plan 绩效计划
     * @return 初始考核阶段
     */
    Integer startPerformanceAssessmentList(HrmPerformancePlanDO plan);

    /**
     * 开启绩效评分
     *
     * @param plan 绩效计划
     * @return 当前评分阶段
     */
    Integer openPerformanceAssessmentScoring(HrmPerformancePlanDO plan);

    /**
     * 发起绩效面谈
     *
     * @param plan 绩效计划
     * @return 下一个考核阶段
     */
    Integer startPerformanceAssessmentInterview(HrmPerformancePlanDO plan);

    /**
     * 归档绩效计划下的员工考核
     *
     * @param plan 绩效计划
     */
    void archivePerformanceAssessmentList(HrmPerformancePlanDO plan);

    /**
     * 判断绩效计划是否可以开启评分
     *
     * @param plan 绩效计划
     * @param assessments 员工绩效考核列表
     * @return 是否可以开启评分
     */
    boolean isPerformanceAssessmentScoringReady(
            HrmPerformancePlanDO plan, List<HrmPerformanceAssessmentDO> assessments);

    /**
     * 判断绩效计划是否可以发起面谈
     *
     * @param plan 绩效计划
     * @param assessments 员工绩效考核列表
     * @param stages 员工绩效考核阶段列表
     * @return 是否可以发起面谈
     */
    boolean isPerformanceAssessmentInterviewReady(
            HrmPerformancePlanDO plan, List<HrmPerformanceAssessmentDO> assessments,
            List<HrmPerformanceAssessmentStageDO> stages);

    /**
     * 判断绩效计划是否可以归档
     *
     * @param plan 绩效计划
     * @param assessments 员工绩效考核列表
     * @return 是否可以归档
     */
    boolean isPerformanceAssessmentArchiveReady(
            HrmPerformancePlanDO plan, List<HrmPerformanceAssessmentDO> assessments);

}
