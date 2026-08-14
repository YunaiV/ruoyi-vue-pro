package cn.iocoder.yudao.module.hrm.service.recruit.candidate;

import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.interview.HrmRecruitInterviewResultReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.interview.HrmRecruitInterviewSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate.HrmRecruitCandidateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate.HrmRecruitInterviewDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.recruit.candidate.HrmRecruitInterviewMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitCandidateStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitInterviewResultEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_INTERVIEW_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_INTERVIEW_FINISHED;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_INTERVIEW_NOT_CURRENT;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_INTERVIEW_RESULT_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_INTERVIEW_STATE_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.MessageTemplateConstants.RECRUIT_INTERVIEW_ARRANGED;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link HrmRecruitInterviewServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmRecruitInterviewServiceImpl.class)
public class HrmRecruitInterviewServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmRecruitInterviewServiceImpl recruitInterviewService;

    @Resource
    private HrmRecruitInterviewMapper recruitInterviewMapper;

    @MockBean
    private HrmRecruitCandidateService recruitCandidateService;
    @MockBean
    private HrmEmployeeService employeeService;
    @MockBean
    private NotifyMessageSendApi notifyMessageSendApi;

    @Test
    public void testCreateRecruitInterview_success() {
        // mock 数据
        Long candidateId = randomLongId();
        HrmRecruitInterviewDO firstInterview = randomRecruitInterviewDO(o -> o
                .setCandidateId(candidateId).setStageNumber(1)
                .setResult(HrmRecruitInterviewResultEnum.PASS.getResult()));
        recruitInterviewMapper.insert(firstInterview);
        // 准备参数
        HrmRecruitInterviewSaveReqVO reqVO = randomRecruitInterviewSaveReqVO(o -> o
                .setCandidateId(candidateId).setStageNumber(null));
        // mock 方法
        when(recruitCandidateService.validateRecruitCandidateExistsForUpdate(eq(candidateId)))
                .thenReturn(randomPojo(HrmRecruitCandidateDO.class, o -> o.setId(candidateId)
                        .setStatus(HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus())));
        when(employeeService.getEmployeeMap(anyCollection())).thenReturn(emptyMap());

        // 调用
        Long interviewId = recruitInterviewService.createRecruitInterview(reqVO);

        // 断言
        assertNotNull(interviewId);
        HrmRecruitInterviewDO interview = recruitInterviewMapper.selectById(interviewId);
        assertPojoEquals(reqVO, interview, "id", "stageNumber");
        assertEquals(firstInterview.getStageNumber() + 1, interview.getStageNumber());
        assertEquals(HrmRecruitInterviewResultEnum.UNFINISHED.getResult(), interview.getResult());
        verify(recruitCandidateService).updateRecruitCandidateInterview(candidateId, 2);
        verify(employeeService).validateEmployeeListByEntryStatus(
                anyCollection(), eq(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus()));
    }

    @Test
    public void testCreateRecruitInterview_employeeNotExists() {
        // 准备参数
        Long candidateId = randomLongId();
        HrmRecruitInterviewSaveReqVO reqVO = randomRecruitInterviewSaveReqVO(o -> o
                .setCandidateId(candidateId));
        // mock 方法
        when(recruitCandidateService.validateRecruitCandidateExistsForUpdate(candidateId))
                .thenReturn(randomPojo(HrmRecruitCandidateDO.class, o -> o.setId(candidateId)
                        .setStatus(HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus())));
        doThrow(exception(EMPLOYEE_NOT_EXISTS)).when(employeeService)
                .validateEmployeeListByEntryStatus(anyCollection(),
                        eq(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> recruitInterviewService.createRecruitInterview(reqVO), EMPLOYEE_NOT_EXISTS);
        assertEquals(0L, recruitInterviewMapper.selectCount());
    }

    @Test
    public void testCreateRecruitInterview_normalizeOtherInterviewEmployeeIds() {
        // 准备参数
        Long candidateId = randomLongId();
        HrmRecruitInterviewSaveReqVO reqVO = randomRecruitInterviewSaveReqVO(o -> o
                .setCandidateId(candidateId).setInterviewEmployeeId(1L)
                .setOtherInterviewEmployeeIds(asList(2L, 1L, 2L, 3L)));
        // mock 方法
        when(recruitCandidateService.validateRecruitCandidateExistsForUpdate(candidateId))
                .thenReturn(randomPojo(HrmRecruitCandidateDO.class, o -> o.setId(candidateId)
                        .setStatus(HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus())));
        when(employeeService.getEmployeeMap(anyCollection())).thenReturn(emptyMap());

        // 调用
        Long interviewId = recruitInterviewService.createRecruitInterview(reqVO);

        // 断言
        assertEquals(asList(2L, 3L),
                recruitInterviewMapper.selectById(interviewId).getOtherInterviewEmployeeIds());
        verify(employeeService).validateEmployeeListByEntryStatus(
                new HashSet<>(asList(1L, 2L, 3L)), HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
    }

    @Test
    public void testCreateRecruitInterview_emptyOtherInterviewEmployeeIds() {
        // 准备参数
        Long candidateId = randomLongId();
        HrmRecruitInterviewSaveReqVO reqVO = randomRecruitInterviewSaveReqVO(o -> o
                .setCandidateId(candidateId).setInterviewEmployeeId(1L)
                .setOtherInterviewEmployeeIds(null));
        // mock 方法
        when(recruitCandidateService.validateRecruitCandidateExistsForUpdate(candidateId))
                .thenReturn(randomPojo(HrmRecruitCandidateDO.class, o -> o.setId(candidateId)
                        .setStatus(HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus())));
        when(employeeService.getEmployeeMap(anyCollection())).thenReturn(emptyMap());

        // 调用
        Long interviewId = recruitInterviewService.createRecruitInterview(reqVO);

        // 断言
        assertEquals(emptyList(),
                recruitInterviewMapper.selectById(interviewId).getOtherInterviewEmployeeIds());
        verify(employeeService).validateEmployeeListByEntryStatus(
                singleton(1L), HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
    }

    @Test
    public void testCreateRecruitInterview_candidateStateInvalid() {
        // 准备参数
        Long candidateId = randomLongId();
        HrmRecruitInterviewSaveReqVO reqVO = randomRecruitInterviewSaveReqVO(o -> o
                .setCandidateId(candidateId));
        // mock 方法
        when(recruitCandidateService.validateRecruitCandidateExistsForUpdate(candidateId))
                .thenReturn(randomPojo(HrmRecruitCandidateDO.class, o -> o.setId(candidateId)
                        .setStatus(HrmRecruitCandidateStatusEnum.OFFER_SENT.getStatus())));

        // 调用，并断言异常
        assertServiceException(() -> recruitInterviewService.createRecruitInterview(reqVO),
                RECRUIT_INTERVIEW_STATE_INVALID);
        assertEquals(0L, recruitInterviewMapper.selectCount());
        verifyNoInteractions(employeeService);
    }

    @Test
    public void testUpdateRecruitInterview_success() {
        // mock 数据
        HrmRecruitInterviewDO dbInterview = randomRecruitInterviewDO(o -> o
                .setResult(HrmRecruitInterviewResultEnum.UNFINISHED.getResult()));
        recruitInterviewMapper.insert(dbInterview);
        // 准备参数
        HrmRecruitInterviewSaveReqVO reqVO = randomRecruitInterviewSaveReqVO(o -> o
                .setId(dbInterview.getId()).setCandidateId(dbInterview.getCandidateId())
                .setStageNumber(randomInteger()));
        // mock 方法
        when(recruitCandidateService.validateRecruitCandidateExistsForUpdate(eq(dbInterview.getCandidateId())))
                .thenReturn(currentInterviewCandidate(dbInterview));
        when(employeeService.getEmployeeMap(anyCollection())).thenReturn(emptyMap());

        // 调用
        recruitInterviewService.updateRecruitInterview(reqVO);

        // 断言
        HrmRecruitInterviewDO interview = recruitInterviewMapper.selectById(dbInterview.getId());
        assertPojoEquals(reqVO, interview, "result", "evaluate", "cancelReason");
        assertEquals(HrmRecruitInterviewResultEnum.UNFINISHED.getResult(), interview.getResult());
        assertNull(interview.getEvaluate());
        assertNull(interview.getCancelReason());
        verify(recruitCandidateService).validateRecruitCandidateExistsForUpdate(dbInterview.getCandidateId());
        verify(recruitCandidateService, never()).updateRecruitCandidateInterview(anyLong(), anyInt());
    }

    @Test
    public void testUpdateRecruitInterview_cancelled() {
        // mock 数据
        HrmRecruitInterviewDO dbInterview = randomRecruitInterviewDO(o -> o
                .setResult(HrmRecruitInterviewResultEnum.CANCEL.getResult())
                .setEvaluate(randomString()).setCancelReason(randomString()));
        recruitInterviewMapper.insert(dbInterview);
        // 准备参数
        HrmRecruitInterviewSaveReqVO reqVO = randomRecruitInterviewSaveReqVO(o -> o
                .setId(dbInterview.getId()).setCandidateId(dbInterview.getCandidateId()));
        // mock 方法
        when(recruitCandidateService.validateRecruitCandidateExistsForUpdate(eq(dbInterview.getCandidateId())))
                .thenReturn(currentInterviewCandidate(dbInterview));
        when(employeeService.getEmployeeMap(anyCollection())).thenReturn(emptyMap());

        // 调用
        recruitInterviewService.updateRecruitInterview(reqVO);

        // 断言
        HrmRecruitInterviewDO interview = recruitInterviewMapper.selectById(dbInterview.getId());
        assertEquals(dbInterview.getStageNumber(), interview.getStageNumber());
        assertEquals(HrmRecruitInterviewResultEnum.UNFINISHED.getResult(), interview.getResult());
        assertNull(interview.getEvaluate());
        assertNull(interview.getCancelReason());
        verify(recruitCandidateService).updateRecruitCandidateInterview(
                dbInterview.getCandidateId(), dbInterview.getStageNumber());
    }

    @Test
    public void testUpdateRecruitInterview_finished() {
        // mock 数据
        HrmRecruitInterviewDO dbInterview = randomRecruitInterviewDO(o -> o
                .setResult(HrmRecruitInterviewResultEnum.PASS.getResult()));
        recruitInterviewMapper.insert(dbInterview);
        // 准备参数
        HrmRecruitInterviewSaveReqVO reqVO = randomRecruitInterviewSaveReqVO(o -> o
                .setId(dbInterview.getId()).setCandidateId(dbInterview.getCandidateId()));
        // mock 方法
        when(recruitCandidateService.validateRecruitCandidateExistsForUpdate(eq(dbInterview.getCandidateId())))
                .thenReturn(currentInterviewCandidate(dbInterview));

        // 调用，并断言异常
        assertServiceException(() -> recruitInterviewService.updateRecruitInterview(reqVO),
                RECRUIT_INTERVIEW_FINISHED);
    }

    @Test
    public void testDeleteRecruitInterview_success() {
        // mock 数据
        HrmRecruitInterviewDO dbInterview = randomRecruitInterviewDO();
        recruitInterviewMapper.insert(dbInterview);
        when(recruitCandidateService.validateRecruitCandidateExistsForUpdate(dbInterview.getCandidateId()))
                .thenReturn(currentInterviewCandidate(dbInterview));

        // 调用
        recruitInterviewService.deleteRecruitInterview(dbInterview.getId());

        // 断言
        assertNull(recruitInterviewMapper.selectById(dbInterview.getId()));
        verify(recruitCandidateService).updateRecruitCandidateInterviewState(
                dbInterview.getCandidateId(), HrmRecruitCandidateStatusEnum.NEW.getStatus(), 0);
    }

    @Test
    public void testDeleteRecruitInterview_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> recruitInterviewService.deleteRecruitInterview(id),
                RECRUIT_INTERVIEW_NOT_EXISTS);
        verifyNoInteractions(recruitCandidateService);
    }

    @Test
    public void testGetRecruitInterview() {
        // mock 数据
        HrmRecruitInterviewDO dbInterview = randomRecruitInterviewDO();
        recruitInterviewMapper.insert(dbInterview);

        // 调用
        HrmRecruitInterviewDO interview = recruitInterviewService.getRecruitInterview(dbInterview.getId());

        // 断言
        assertPojoEquals(dbInterview, interview);
        verifyNoInteractions(recruitCandidateService);
    }

    @Test
    public void testGetRecruitInterviewListByCandidateId() {
        // mock 数据
        Long candidateId = randomLongId();
        HrmRecruitInterviewDO firstInterview = randomRecruitInterviewDO(o -> o
                .setCandidateId(candidateId).setStageNumber(1));
        recruitInterviewMapper.insert(firstInterview);
        HrmRecruitInterviewDO secondInterview = randomRecruitInterviewDO(o -> o
                .setCandidateId(candidateId).setStageNumber(2));
        recruitInterviewMapper.insert(secondInterview);
        // 测试 candidateId 不匹配
        recruitInterviewMapper.insert(randomRecruitInterviewDO());

        // 调用
        List<HrmRecruitInterviewDO> interviews = recruitInterviewService
                .getRecruitInterviewListByCandidateId(candidateId);

        // 断言
        assertEquals(asList(secondInterview.getId(), firstInterview.getId()),
                convertList(interviews, HrmRecruitInterviewDO::getId));
        verifyNoInteractions(recruitCandidateService);
    }

    @Test
    public void testGetLatestRecruitInterviewMapByCandidateIds() {
        // mock 数据
        Long firstCandidateId = randomLongId();
        HrmRecruitInterviewDO historyInterview = randomRecruitInterviewDO(o -> o
                .setCandidateId(firstCandidateId).setStageNumber(1));
        recruitInterviewMapper.insert(historyInterview);
        HrmRecruitInterviewDO latestInterview = randomRecruitInterviewDO(o -> o
                .setCandidateId(firstCandidateId).setStageNumber(2));
        recruitInterviewMapper.insert(latestInterview);
        Long secondCandidateId = randomLongId();
        HrmRecruitInterviewDO secondInterview = randomRecruitInterviewDO(o -> o
                .setCandidateId(secondCandidateId).setStageNumber(1));
        recruitInterviewMapper.insert(secondInterview);

        // 调用
        Map<Long, HrmRecruitInterviewDO> interviewMap = recruitInterviewService
                .getLatestRecruitInterviewMapByCandidateIds(asList(firstCandidateId, secondCandidateId));

        // 断言
        assertEquals(2, interviewMap.size());
        assertPojoEquals(latestInterview, interviewMap.get(firstCandidateId));
        assertPojoEquals(secondInterview, interviewMap.get(secondCandidateId));
    }

    @Test
    public void testGetLatestRecruitInterviewMapByCandidateIds_empty() {
        // 调用
        Map<Long, HrmRecruitInterviewDO> interviewMap = recruitInterviewService
                .getLatestRecruitInterviewMapByCandidateIds(emptyList());

        // 断言
        assertTrue(interviewMap.isEmpty());
    }

    @Test
    public void testDeleteRecruitInterviewByCandidateId_success() {
        // mock 数据
        Long candidateId = randomLongId();
        HrmRecruitInterviewDO firstInterview = randomRecruitInterviewDO(o -> o.setCandidateId(candidateId));
        recruitInterviewMapper.insert(firstInterview);
        HrmRecruitInterviewDO secondInterview = randomRecruitInterviewDO(o -> o.setCandidateId(candidateId));
        recruitInterviewMapper.insert(secondInterview);
        // 测试 candidateId 不匹配
        HrmRecruitInterviewDO retainedInterview = randomRecruitInterviewDO();
        recruitInterviewMapper.insert(retainedInterview);

        // 调用
        recruitInterviewService.deleteRecruitInterviewByCandidateId(candidateId);

        // 断言
        assertNull(recruitInterviewMapper.selectById(firstInterview.getId()));
        assertNull(recruitInterviewMapper.selectById(secondInterview.getId()));
        assertNotNull(recruitInterviewMapper.selectById(retainedInterview.getId()));
    }

    @Test
    public void testFinishCurrentRecruitInterviewForElimination_success() {
        // mock 数据
        HrmRecruitInterviewDO interview = randomRecruitInterviewDO();
        recruitInterviewMapper.insert(interview);

        // 调用
        recruitInterviewService.finishCurrentRecruitInterviewForElimination(
                interview.getCandidateId(), "专业能力不匹配");

        // 断言
        HrmRecruitInterviewDO dbInterview = recruitInterviewMapper.selectById(interview.getId());
        assertEquals(HrmRecruitInterviewResultEnum.NOT_PASS.getResult(), dbInterview.getResult());
        assertEquals("专业能力不匹配", dbInterview.getEvaluate());
        assertNull(dbInterview.getCancelReason());
    }

    @Test
    public void testUpdateRecruitInterviewResult_pass() {
        testUpdateRecruitInterviewResult(HrmRecruitInterviewResultEnum.PASS.getResult(),
                HrmRecruitCandidateStatusEnum.INTERVIEW_PASS.getStatus());
    }

    @Test
    public void testUpdateRecruitInterviewResult_notPass() {
        testUpdateRecruitInterviewResult(HrmRecruitInterviewResultEnum.NOT_PASS.getResult(),
                HrmRecruitCandidateStatusEnum.ELIMINATED.getStatus());
    }

    private void testUpdateRecruitInterviewResult(Integer interviewResult, Integer candidateStatus) {
        // mock 数据
        HrmRecruitInterviewDO dbInterview = randomRecruitInterviewDO();
        recruitInterviewMapper.insert(dbInterview);
        // 准备参数
        HrmRecruitInterviewResultReqVO reqVO = randomPojo(HrmRecruitInterviewResultReqVO.class, o -> o
                .setId(dbInterview.getId()).setResult(interviewResult));
        // mock 方法
        when(recruitCandidateService.validateRecruitCandidateExistsForUpdate(eq(dbInterview.getCandidateId())))
                .thenReturn(currentInterviewCandidate(dbInterview));

        // 调用
        recruitInterviewService.updateRecruitInterviewResult(reqVO);

        // 断言
        HrmRecruitInterviewDO interview = recruitInterviewMapper.selectById(dbInterview.getId());
        assertPojoEquals(reqVO, interview);
        verify(recruitCandidateService).updateRecruitCandidateStatusByInterviewResult(
                dbInterview.getCandidateId(), candidateStatus);
    }

    @Test
    public void testUpdateRecruitInterviewResult_cancel() {
        // mock 数据
        HrmRecruitInterviewDO dbInterview = randomRecruitInterviewDO();
        recruitInterviewMapper.insert(dbInterview);
        // 准备参数
        HrmRecruitInterviewResultReqVO reqVO = randomPojo(HrmRecruitInterviewResultReqVO.class, o -> o
                .setId(dbInterview.getId()).setResult(HrmRecruitInterviewResultEnum.CANCEL.getResult())
                .setCancelReason(randomString()));
        // mock 方法
        when(recruitCandidateService.validateRecruitCandidateExistsForUpdate(eq(dbInterview.getCandidateId())))
                .thenReturn(currentInterviewCandidate(dbInterview));

        // 调用
        recruitInterviewService.updateRecruitInterviewResult(reqVO);

        // 断言
        HrmRecruitInterviewDO interview = recruitInterviewMapper.selectById(dbInterview.getId());
        assertPojoEquals(reqVO, interview);
        verify(recruitCandidateService, never()).updateRecruitCandidateStatusByInterviewResult(anyLong(), anyInt());
        verify(recruitCandidateService).updateRecruitCandidateInterviewState(
                dbInterview.getCandidateId(), HrmRecruitCandidateStatusEnum.INTERVIEW.getStatus(),
                dbInterview.getStageNumber());
    }

    @Test
    public void testUpdateRecruitInterviewResult_unfinishedResult() {
        // mock 数据
        HrmRecruitInterviewDO dbInterview = randomRecruitInterviewDO();
        recruitInterviewMapper.insert(dbInterview);
        when(recruitCandidateService.validateRecruitCandidateExistsForUpdate(eq(dbInterview.getCandidateId())))
                .thenReturn(currentInterviewCandidate(dbInterview));
        // 准备参数
        HrmRecruitInterviewResultReqVO reqVO = new HrmRecruitInterviewResultReqVO()
                .setId(dbInterview.getId()).setResult(HrmRecruitInterviewResultEnum.UNFINISHED.getResult());

        // 调用，并断言异常
        assertServiceException(() -> recruitInterviewService.updateRecruitInterviewResult(reqVO),
                RECRUIT_INTERVIEW_RESULT_INVALID);
        verify(recruitCandidateService, never()).updateRecruitCandidateStatusByInterviewResult(anyLong(), anyInt());
    }

    @Test
    public void testUpdateRecruitInterviewResult_historicalInterview() {
        // mock 数据
        Long candidateId = randomLongId();
        HrmRecruitInterviewDO historicalInterview = randomRecruitInterviewDO(o -> o
                .setCandidateId(candidateId).setStageNumber(1));
        recruitInterviewMapper.insert(historicalInterview);
        HrmRecruitInterviewDO currentInterview = randomRecruitInterviewDO(o -> o
                .setCandidateId(candidateId).setStageNumber(2));
        recruitInterviewMapper.insert(currentInterview);
        when(recruitCandidateService.validateRecruitCandidateExistsForUpdate(candidateId))
                .thenReturn(currentInterviewCandidate(currentInterview));
        HrmRecruitInterviewResultReqVO reqVO = new HrmRecruitInterviewResultReqVO()
                .setId(historicalInterview.getId()).setResult(HrmRecruitInterviewResultEnum.PASS.getResult());

        // 调用、断言
        assertServiceException(() -> recruitInterviewService.updateRecruitInterviewResult(reqVO),
                RECRUIT_INTERVIEW_NOT_CURRENT);
        assertEquals(HrmRecruitInterviewResultEnum.UNFINISHED.getResult(),
                recruitInterviewMapper.selectById(historicalInterview.getId()).getResult());
        verify(recruitCandidateService, never()).updateRecruitCandidateStatusByInterviewResult(anyLong(), anyInt());
    }

    @Test
    public void testCreateRecruitInterview_sendNotify() {
        // 准备参数
        Long candidateId = randomLongId();
        HrmRecruitInterviewSaveReqVO reqVO = randomRecruitInterviewSaveReqVO(o -> o
                .setCandidateId(candidateId)
                .setInterviewEmployeeId(1L).setOtherInterviewEmployeeIds(asList(2L, 3L)));
        // mock 方法
        HrmRecruitCandidateDO candidate = randomPojo(HrmRecruitCandidateDO.class, o -> o
                .setId(candidateId).setName(randomString())
                .setStatus(HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus()));
        when(recruitCandidateService.validateRecruitCandidateExistsForUpdate(eq(candidateId))).thenReturn(candidate);
        Map<Long, HrmEmployeeDO> employeeMap = new HashMap<>();
        employeeMap.put(1L, randomPojo(HrmEmployeeDO.class, o -> o.setId(1L).setUserId(11L)));
        employeeMap.put(2L, randomPojo(HrmEmployeeDO.class, o -> o.setId(2L).setUserId(12L)));
        employeeMap.put(3L, randomPojo(HrmEmployeeDO.class, o -> o.setId(3L).setUserId(11L)));
        when(employeeService.getEmployeeMap(anyCollection())).thenReturn(employeeMap);

        // 调用
        recruitInterviewService.createRecruitInterview(reqVO);

        // 断言
        ArgumentCaptor<NotifySendSingleToUserReqDTO> notifyCaptor =
                ArgumentCaptor.forClass(NotifySendSingleToUserReqDTO.class);
        verify(notifyMessageSendApi, times(2)).sendSingleMessageToAdmin(notifyCaptor.capture());
        assertEquals(RECRUIT_INTERVIEW_ARRANGED, notifyCaptor.getAllValues().get(0).getTemplateCode());
        assertEquals(candidate.getName(),
                notifyCaptor.getAllValues().get(0).getTemplateParams().get("candidateName"));
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static HrmRecruitInterviewDO randomRecruitInterviewDO(Consumer<HrmRecruitInterviewDO>... consumers) {
        Consumer<HrmRecruitInterviewDO> consumer = o -> o.setType(1).setStageNumber(1)
                .setOtherInterviewEmployeeIds(emptyList())
                .setResult(HrmRecruitInterviewResultEnum.UNFINISHED.getResult());
        return randomPojo(HrmRecruitInterviewDO.class, ArrayUtils.append(consumer, consumers));
    }

    @SafeVarargs
    private static HrmRecruitInterviewSaveReqVO randomRecruitInterviewSaveReqVO(
            Consumer<HrmRecruitInterviewSaveReqVO>... consumers) {
        Consumer<HrmRecruitInterviewSaveReqVO> consumer = o -> o.setId(null).setType(1).setStageNumber(null)
                .setOtherInterviewEmployeeIds(singletonList(randomLongId()));
        return randomPojo(HrmRecruitInterviewSaveReqVO.class, ArrayUtils.append(consumer, consumers));
    }

    private static HrmRecruitCandidateDO currentInterviewCandidate(HrmRecruitInterviewDO interview) {
        return randomPojo(HrmRecruitCandidateDO.class, candidate -> candidate
                .setId(interview.getCandidateId())
                .setStatus(HrmRecruitCandidateStatusEnum.INTERVIEW.getStatus())
                .setStageNumber(interview.getStageNumber()));
    }

}
