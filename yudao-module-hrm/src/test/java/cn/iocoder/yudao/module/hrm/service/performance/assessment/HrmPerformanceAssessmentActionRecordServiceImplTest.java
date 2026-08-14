package cn.iocoder.yudao.module.hrm.service.performance.assessment;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentActionRecordDO;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentActionTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentStageStatusEnum;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link HrmPerformanceAssessmentActionRecordServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmPerformanceAssessmentActionRecordServiceImpl.class)
public class HrmPerformanceAssessmentActionRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmPerformanceAssessmentActionRecordServiceImpl actionRecordService;

    @Test
    public void testCreateListAndDeletePerformanceAssessmentActionRecord() {
        // mock 数据
        Long employeeId = randomLongId();
        Long assessmentId = randomLongId();
        Long stageId = randomLongId();

        // 调用
        actionRecordService.createPerformanceAssessmentActionRecord(
                employeeId, assessmentId, stageId,
                HrmPerformanceAssessmentActionTypeEnum.SUBMIT_APPEAL,
                Arrays.asList("https://example.com/a.pdf", "https://example.com/b.png"),
                HrmPerformanceAssessmentStageStatusEnum.APPEALED.getStatus(), "申请复核评分结果");
        actionRecordService.createPerformanceAssessmentActionRecord(
                null, assessmentId, stageId,
                HrmPerformanceAssessmentActionTypeEnum.APPEAL_TIMEOUT_REJECT,
                null, HrmPerformanceAssessmentStageStatusEnum.REJECTED.getStatus(),
                "，意见：申诉确认超期，系统自动驳回");

        // 断言创建和查询
        List<HrmPerformanceAssessmentActionRecordDO> records = actionRecordService
                .getPerformanceAssessmentActionRecordList(assessmentId);
        assertEquals(2, records.size());
        assertEquals(employeeId, records.get(0).getEmployeeId());
        assertEquals("提交绩效申诉", records.get(0).getTitle());
        assertEquals("提交了绩效申诉，原因：申请复核评分结果", records.get(0).getContent());
        assertEquals(Arrays.asList("https://example.com/a.pdf", "https://example.com/b.png"),
                records.get(0).getFileUrls());
        assertEquals(HrmPerformanceAssessmentActionTypeEnum.APPEAL_TIMEOUT_REJECT.getType(),
                records.get(1).getType());

        // 调用并断言删除
        actionRecordService.deletePerformanceAssessmentActionRecordList(
                Collections.singleton(assessmentId));
        assertTrue(actionRecordService.getPerformanceAssessmentActionRecordList(assessmentId).isEmpty());
    }

}
