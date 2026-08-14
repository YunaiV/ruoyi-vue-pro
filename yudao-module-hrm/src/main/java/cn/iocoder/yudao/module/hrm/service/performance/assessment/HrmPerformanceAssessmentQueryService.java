package cn.iocoder.yudao.module.hrm.service.performance.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceArchiveEmployeeRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceProcessRecordRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentStageDO;

import java.util.List;

/**
 * HRM 员工绩效考核查询 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmPerformanceAssessmentQueryService {

    /**
     * 获得员工绩效考核响应
     *
     * @param assessment 员工绩效考核
     * @return 员工绩效考核响应
     */
    HrmPerformanceAssessmentRespVO getPerformanceAssessmentRespVO(HrmPerformanceAssessmentDO assessment);

    /**
     * 获得员工绩效考核分页响应
     *
     * @param pageResult 员工绩效考核分页
     * @return 员工绩效考核分页响应
     */
    PageResult<HrmPerformanceAssessmentRespVO> getPerformanceAssessmentRespVOPage(
            PageResult<HrmPerformanceAssessmentDO> pageResult);

    /**
     * 获得员工绩效考核响应列表
     *
     * @param assessments 员工绩效考核列表
     * @return 员工绩效考核响应列表
     */
    List<HrmPerformanceAssessmentRespVO> getPerformanceAssessmentRespVOList(
            List<HrmPerformanceAssessmentDO> assessments);

    /**
     * 获得当前用户可操作的员工绩效考核响应
     *
     * @param assessment 员工绩效考核
     * @param userId 用户编号
     * @return 员工绩效考核响应
     */
    HrmPerformanceAssessmentRespVO getPerformanceAssessmentProcessRespVO(
            HrmPerformanceAssessmentDO assessment, Long userId);

    /**
     * 获得当前用户可操作的员工绩效考核响应列表
     *
     * @param assessments 员工绩效考核列表
     * @param userId 用户编号
     * @return 员工绩效考核响应列表
     */
    List<HrmPerformanceAssessmentRespVO> getPerformanceAssessmentProcessRespVOList(
            List<HrmPerformanceAssessmentDO> assessments, Long userId);

    /**
     * 获得绩效任务响应
     *
     * @param assessment 员工绩效考核
     * @param stageId 任务阶段编号
     * @param userId 用户编号
     * @return 员工绩效考核响应
     */
    HrmPerformanceAssessmentRespVO getPerformanceAssessmentTaskRespVO(
            HrmPerformanceAssessmentDO assessment, Long stageId, Long userId);

    /**
     * 获得绩效任务响应列表
     *
     * @param assessments 员工绩效考核列表
     * @param stageIds 任务阶段编号列表
     * @param userId 用户编号
     * @return 员工绩效考核响应列表
     */
    List<HrmPerformanceAssessmentRespVO> getPerformanceAssessmentTaskRespVOList(
            List<HrmPerformanceAssessmentDO> assessments, List<Long> stageIds, Long userId);

    /**
     * 获得绩效任务分页响应
     *
     * @param pageResult 绩效阶段任务分页
     * @param userId 用户编号
     * @return 绩效任务分页响应
     */
    PageResult<HrmPerformanceAssessmentRespVO> getPerformanceAssessmentTaskRespVOPage(
            PageResult<HrmPerformanceAssessmentStageDO> pageResult, Long userId);

    /**
     * 获得绩效流程记录列表
     *
     * @param assessment 员工绩效考核
     * @return 绩效流程记录列表
     */
    List<HrmPerformanceProcessRecordRespVO> getPerformanceAssessmentProcessRecordList(
            HrmPerformanceAssessmentDO assessment);

    /**
     * 获得员工端可见的绩效流程记录列表
     *
     * @param assessment 员工绩效考核
     * @param userId 用户编号
     * @return 绩效流程记录列表
     */
    List<HrmPerformanceProcessRecordRespVO> getPerformanceAssessmentProcessRecordList(
            HrmPerformanceAssessmentDO assessment, Long userId);

    /**
     * 获得员工绩效档案分页响应
     *
     * @param pageResult 员工分页
     * @return 员工绩效档案分页响应
     */
    PageResult<HrmPerformanceArchiveEmployeeRespVO> getPerformanceArchiveEmployeeRespVOPage(
            PageResult<HrmEmployeeDO> pageResult);

}
