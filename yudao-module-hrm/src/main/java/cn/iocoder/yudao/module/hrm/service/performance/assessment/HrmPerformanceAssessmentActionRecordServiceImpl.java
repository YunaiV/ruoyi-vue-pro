package cn.iocoder.yudao.module.hrm.service.performance.assessment;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentActionRecordDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentActionRecordMapper;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentActionTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * HRM 员工绩效考核动作记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class HrmPerformanceAssessmentActionRecordServiceImpl
        implements HrmPerformanceAssessmentActionRecordService {

    @Resource
    private HrmPerformanceAssessmentActionRecordMapper assessmentActionRecordMapper;

    @Override
    public void createPerformanceAssessmentActionRecord(
            Long employeeId, Long assessmentId, Long stageId,
            HrmPerformanceAssessmentActionTypeEnum actionType,
            List<String> fileUrls, Integer status, Object... contentArgs) {
        assessmentActionRecordMapper.insert(new HrmPerformanceAssessmentActionRecordDO()
                .setAssessmentId(assessmentId).setStageId(stageId)
                .setEmployeeId(employeeId).setType(actionType.getType())
                .setTitle(actionType.getTitle()).setContent(actionType.formatContent(contentArgs))
                .setFileUrls(CollUtil.isEmpty(fileUrls) ? null : fileUrls).setStatus(status));
    }

    @Override
    public List<HrmPerformanceAssessmentActionRecordDO> getPerformanceAssessmentActionRecordList(
            Long assessmentId) {
        return assessmentId == null ? Collections.emptyList()
                : assessmentActionRecordMapper.selectListByAssessmentId(assessmentId);
    }

    @Override
    public void deletePerformanceAssessmentActionRecordList(Collection<Long> assessmentIds) {
        if (CollUtil.isEmpty(assessmentIds)) {
            return;
        }
        assessmentActionRecordMapper.deleteByAssessmentIds(assessmentIds);
    }

}
