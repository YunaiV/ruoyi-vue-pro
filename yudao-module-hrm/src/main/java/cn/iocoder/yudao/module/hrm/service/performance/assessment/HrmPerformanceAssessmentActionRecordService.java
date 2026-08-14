package cn.iocoder.yudao.module.hrm.service.performance.assessment;

import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentActionRecordDO;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentActionTypeEnum;

import java.util.Collection;
import java.util.List;

/**
 * HRM 员工绩效考核动作记录 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmPerformanceAssessmentActionRecordService {

    /**
     * 创建员工绩效考核动作记录
     *
     * @param employeeId 操作员工编号；系统自动动作时为空
     * @param assessmentId 员工绩效考核编号
     * @param stageId 员工绩效考核阶段编号
     * @param actionType 动作类型
     * @param fileUrls 附件地址列表
     * @param status 动作完成后的阶段状态
     * @param contentArgs 动作内容参数
     */
    void createPerformanceAssessmentActionRecord(
            Long employeeId, Long assessmentId, Long stageId,
            HrmPerformanceAssessmentActionTypeEnum actionType,
            List<String> fileUrls, Integer status, Object... contentArgs);

    /**
     * 获得员工绩效考核动作记录列表
     *
     * @param assessmentId 员工绩效考核编号
     * @return 动作记录列表
     */
    List<HrmPerformanceAssessmentActionRecordDO> getPerformanceAssessmentActionRecordList(Long assessmentId);

    /**
     * 删除员工绩效考核动作记录
     *
     * @param assessmentIds 员工绩效考核编号集合
     */
    void deletePerformanceAssessmentActionRecordList(Collection<Long> assessmentIds);

}
