package cn.iocoder.yudao.module.hrm.service.recruit.candidate;

import cn.iocoder.yudao.module.hrm.service.recruit.config.HrmRecruitChannelService;
import cn.iocoder.yudao.module.hrm.service.recruit.post.HrmRecruitPostService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateEntryReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidatePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateUpdateChannelReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateUpdateEliminateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateUpdatePostReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateUpdateStatusReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate.HrmRecruitCandidateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.config.HrmRecruitChannelDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate.HrmRecruitInterviewDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.post.HrmRecruitPostDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.recruit.candidate.HrmRecruitCandidateMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.recruit.candidate.HrmRecruitInterviewMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.recruit.post.HrmRecruitPostMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitCandidateStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitInterviewResultEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.post.HrmRecruitPostStatusEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_CANDIDATE_CONVERTED;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CANDIDATE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CANDIDATE_CONVERT_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CANDIDATE_DELETE_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CANDIDATE_ELIMINATE_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CANDIDATE_HAS_EMPLOYEE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CANDIDATE_STATUS_TRANSITION_INVALID;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link HrmRecruitCandidateServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmRecruitCandidateServiceImpl.class)
public class HrmRecruitCandidateServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmRecruitCandidateServiceImpl recruitCandidateService;

    @Resource
    private HrmRecruitCandidateMapper recruitCandidateMapper;
    @Resource
    private HrmRecruitPostMapper recruitPostMapper;
    @Resource
    private HrmRecruitInterviewMapper recruitInterviewMapper;

    @MockitoBean
    private HrmRecruitPostService recruitPostService;
    @MockitoBean
    private HrmRecruitChannelService recruitChannelService;
    @MockitoBean
    private HrmRecruitInterviewService recruitInterviewService;
    @MockitoBean
    private HrmEmployeeService employeeService;

    @Test
    public void testCreateRecruitCandidate_success() {
        // 准备参数
        HrmRecruitCandidateSaveReqVO reqVO = randomRecruitCandidateSaveReqVO();
        // mock 方法
        when(recruitPostService.validateRecruitPostExists(eq(reqVO.getPostId())))
                .thenReturn(randomRecruitPostDO(o -> o.setId(reqVO.getPostId())));
        when(recruitChannelService.validateRecruitChannelExists(eq(reqVO.getChannelId())))
                .thenReturn(randomPojo(HrmRecruitChannelDO.class, o -> o.setId(reqVO.getChannelId())));

        // 调用
        Long candidateId = recruitCandidateService.createRecruitCandidate(reqVO);

        // 断言
        assertNotNull(candidateId);
        HrmRecruitCandidateDO candidate = recruitCandidateMapper.selectById(candidateId);
        assertPojoEquals(reqVO, candidate, "id");
        assertEquals(HrmRecruitCandidateStatusEnum.NEW.getStatus(), candidate.getStatus());
        assertEquals(0, candidate.getStageNumber());
        assertNotNull(candidate.getStatusUpdateTime());
    }

    @Test
    public void testUpdateRecruitCandidate_success() {
        // mock 数据
        HrmRecruitCandidateDO dbCandidate = randomRecruitCandidateDO();
        recruitCandidateMapper.insert(dbCandidate);
        // 准备参数
        HrmRecruitCandidateSaveReqVO reqVO = randomRecruitCandidateSaveReqVO(o -> o.setId(dbCandidate.getId()));
        // mock 方法
        when(recruitPostService.validateRecruitPostExists(eq(reqVO.getPostId())))
                .thenReturn(randomRecruitPostDO(o -> o.setId(reqVO.getPostId())));
        when(recruitChannelService.validateRecruitChannelExists(eq(reqVO.getChannelId())))
                .thenReturn(randomPojo(HrmRecruitChannelDO.class, o -> o.setId(reqVO.getChannelId())));

        // 调用
        recruitCandidateService.updateRecruitCandidate(reqVO);

        // 断言
        HrmRecruitCandidateDO candidate = recruitCandidateMapper.selectById(dbCandidate.getId());
        assertPojoEquals(reqVO, candidate);
    }

    @Test
    public void testUpdateRecruitCandidate_clearOptionalFieldsAndKeepEntryTime() {
        // mock 数据
        LocalDateTime entryTime = LocalDateTime.of(2026, 7, 1, 9, 0);
        HrmRecruitCandidateDO dbCandidate = randomRecruitCandidateDO(o -> o.setEntryTime(entryTime));
        recruitCandidateMapper.insert(dbCandidate);
        // 准备参数
        HrmRecruitCandidateSaveReqVO reqVO = randomRecruitCandidateSaveReqVO(o -> o
                .setId(dbCandidate.getId()).setAge(null).setEmail(null).setWorkTime(null)
                .setGraduateSchool(null).setLatestWorkPlace(null).setChannelId(null)
                .setRemark(null).setResumeUrls(null));
        when(recruitPostService.validateRecruitPostExists(eq(reqVO.getPostId())))
                .thenReturn(randomRecruitPostDO(o -> o.setId(reqVO.getPostId())));

        // 调用
        recruitCandidateService.updateRecruitCandidate(reqVO);

        // 断言
        HrmRecruitCandidateDO candidate = recruitCandidateMapper.selectById(dbCandidate.getId());
        assertNull(candidate.getAge());
        assertNull(candidate.getEmail());
        assertNull(candidate.getWorkTime());
        assertNull(candidate.getGraduateSchool());
        assertNull(candidate.getLatestWorkPlace());
        assertNull(candidate.getChannelId());
        assertNull(candidate.getRemark());
        assertNull(candidate.getResumeUrls());
        assertEquals(entryTime, candidate.getEntryTime());
        assertEquals(dbCandidate.getStatus(), candidate.getStatus());
    }

    @Test
    public void testDeleteRecruitCandidate_success() {
        // mock 数据
        HrmRecruitCandidateDO dbCandidate = randomRecruitCandidateDO();
        recruitCandidateMapper.insert(dbCandidate);

        // 调用
        recruitCandidateService.deleteRecruitCandidate(dbCandidate.getId());

        // 断言
        assertNull(recruitCandidateMapper.selectById(dbCandidate.getId()));
        verify(recruitInterviewService).deleteRecruitInterviewByCandidateId(dbCandidate.getId());
    }

    @Test
    public void testDeleteRecruitCandidate_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> recruitCandidateService.deleteRecruitCandidate(id),
                RECRUIT_CANDIDATE_NOT_EXISTS);
        verifyNoInteractions(recruitInterviewService);
    }

    @Test
    public void testDeleteRecruitCandidate_hasEmployee() {
        // mock 数据
        HrmRecruitCandidateDO candidate = randomRecruitCandidateDO(
                o -> o.setStatus(HrmRecruitCandidateStatusEnum.INTERVIEW_PASS.getStatus()));
        recruitCandidateMapper.insert(candidate);
        when(employeeService.getEmployeeByCandidateId(candidate.getId()))
                .thenReturn(new HrmEmployeeDO().setId(randomLongId()).setCandidateId(candidate.getId()));

        // 调用、断言
        assertServiceException(() -> recruitCandidateService.deleteRecruitCandidate(candidate.getId()),
                RECRUIT_CANDIDATE_HAS_EMPLOYEE);
        assertNotNull(recruitCandidateMapper.selectById(candidate.getId()));
        verifyNoInteractions(recruitInterviewService);
    }

    @Test
    public void testDeleteRecruitCandidate_statusInvalid() {
        // mock 数据
        HrmRecruitCandidateDO candidate = randomRecruitCandidateDO(
                o -> o.setStatus(HrmRecruitCandidateStatusEnum.OFFER_SENT.getStatus()));
        recruitCandidateMapper.insert(candidate);

        // 调用、断言
        assertServiceException(() -> recruitCandidateService.deleteRecruitCandidate(candidate.getId()),
                RECRUIT_CANDIDATE_DELETE_STATUS_INVALID);
        assertNotNull(recruitCandidateMapper.selectById(candidate.getId()));
        verifyNoInteractions(recruitInterviewService, employeeService);
    }

    @Test
    public void testGetRecruitCandidate() {
        // mock 数据
        HrmRecruitCandidateDO dbCandidate = randomRecruitCandidateDO();
        recruitCandidateMapper.insert(dbCandidate);

        // 调用
        HrmRecruitCandidateDO candidate = recruitCandidateService.getRecruitCandidate(dbCandidate.getId());

        // 断言
        assertPojoEquals(dbCandidate, candidate);
    }

    @Test
    public void testGetRecruitCandidate_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用
        HrmRecruitCandidateDO candidate = recruitCandidateService.getRecruitCandidate(id);

        // 断言
        assertNull(candidate);
    }

    @Test
    public void testGetRecruitCandidatePage_joinFilters() {
        // mock 职位数据
        HrmRecruitPostDO targetPost = randomRecruitPostDO(o -> o.setOwnerEmployeeId(100L));
        recruitPostMapper.insert(targetPost);
        HrmRecruitPostDO otherOwnerPost = cloneIgnoreId(targetPost, o -> o.setOwnerEmployeeId(200L));
        recruitPostMapper.insert(otherOwnerPost);
        // mock 候选人及面试数据
        LocalDateTime interviewTime = LocalDateTime.of(2026, 7, 17, 10, 0);
        HrmRecruitCandidateDO targetCandidate = randomRecruitCandidateDO(o -> o
                .setPostId(targetPost.getId()).setStageNumber(2)
                .setStatus(HrmRecruitCandidateStatusEnum.INTERVIEW.getStatus()));
        recruitCandidateMapper.insert(targetCandidate);
        recruitInterviewMapper.insert(randomRecruitInterviewDO(o -> o
                .setCandidateId(targetCandidate.getId()).setStageNumber(2)
                .setInterviewEmployeeId(10L).setInterviewTime(interviewTime)));
        recruitInterviewMapper.insert(randomRecruitInterviewDO(o -> o
                .setCandidateId(targetCandidate.getId()).setStageNumber(2)
                .setInterviewEmployeeId(11L).setOtherInterviewEmployeeIds(singletonList(10L))
                .setInterviewTime(interviewTime.plusHours(1))
                .setResult(HrmRecruitInterviewResultEnum.PASS.getResult())));
        // 测试面试轮次不匹配
        HrmRecruitCandidateDO historyOnlyCandidate = cloneIgnoreId(targetCandidate, o -> o.setName("历史轮次"));
        recruitCandidateMapper.insert(historyOnlyCandidate);
        recruitInterviewMapper.insert(randomRecruitInterviewDO(o -> o
                .setCandidateId(historyOnlyCandidate.getId()).setStageNumber(1)
                .setInterviewEmployeeId(10L).setInterviewTime(interviewTime)));
        // 测试面试已取消
        HrmRecruitCandidateDO canceledCandidate = cloneIgnoreId(targetCandidate, o -> o.setName("面试取消"));
        recruitCandidateMapper.insert(canceledCandidate);
        recruitInterviewMapper.insert(randomRecruitInterviewDO(o -> o
                .setCandidateId(canceledCandidate.getId()).setStageNumber(2)
                .setInterviewEmployeeId(10L).setInterviewTime(interviewTime)
                .setResult(HrmRecruitInterviewResultEnum.CANCEL.getResult())));
        // 测试招聘负责人不匹配
        HrmRecruitCandidateDO otherOwnerCandidate = cloneIgnoreId(targetCandidate,
                o -> o.setName("负责人不匹配").setPostId(otherOwnerPost.getId()));
        recruitCandidateMapper.insert(otherOwnerCandidate);
        recruitInterviewMapper.insert(randomRecruitInterviewDO(o -> o
                .setCandidateId(otherOwnerCandidate.getId()).setStageNumber(2)
                .setInterviewEmployeeId(10L).setInterviewTime(interviewTime)));
        // 准备参数
        HrmRecruitCandidatePageReqVO reqVO = new HrmRecruitCandidatePageReqVO()
                .setOwnerEmployeeId(100L).setInterviewEmployeeId(10L)
                .setInterviewTime(new LocalDateTime[]{interviewTime.minusDays(1), interviewTime.plusDays(1)});

        // 调用
        PageResult<HrmRecruitCandidateDO> pageResult = recruitCandidateService.getRecruitCandidatePage(reqVO);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(targetCandidate, pageResult.getList().get(0));
    }

    @Test
    public void testUpdateRecruitCandidateInterview_success() {
        // mock 数据
        HrmRecruitCandidateDO dbCandidate = randomRecruitCandidateDO();
        recruitCandidateMapper.insert(dbCandidate);
        // 准备参数
        Integer stageNumber = 2;

        // 调用
        recruitCandidateService.updateRecruitCandidateInterview(dbCandidate.getId(), stageNumber);

        // 断言
        HrmRecruitCandidateDO candidate = recruitCandidateMapper.selectById(dbCandidate.getId());
        assertEquals(HrmRecruitCandidateStatusEnum.INTERVIEW.getStatus(), candidate.getStatus());
        assertEquals(stageNumber, candidate.getStageNumber());
        assertNotNull(candidate.getStatusUpdateTime());
    }

    @Test
    public void testUpdateRecruitCandidateStatusByInterviewResult_success() {
        // mock 数据
        HrmRecruitCandidateDO dbCandidate = randomRecruitCandidateDO();
        recruitCandidateMapper.insert(dbCandidate);
        // 准备参数
        Integer status = HrmRecruitCandidateStatusEnum.INTERVIEW_PASS.getStatus();

        // 调用
        recruitCandidateService.updateRecruitCandidateStatusByInterviewResult(dbCandidate.getId(), status);

        // 断言
        HrmRecruitCandidateDO candidate = recruitCandidateMapper.selectById(dbCandidate.getId());
        assertEquals(status, candidate.getStatus());
        assertNotNull(candidate.getStatusUpdateTime());
    }

    @Test
    public void testUpdateRecruitCandidateStatus_success() {
        // mock 数据
        HrmRecruitCandidateDO dbCandidate = randomRecruitCandidateDO();
        recruitCandidateMapper.insert(dbCandidate);
        // 准备参数
        HrmRecruitCandidateUpdateStatusReqVO reqVO = randomPojo(HrmRecruitCandidateUpdateStatusReqVO.class, o -> o
                .setId(dbCandidate.getId()).setStatus(HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus()));

        // 调用
        recruitCandidateService.updateRecruitCandidateStatus(reqVO);

        // 断言
        HrmRecruitCandidateDO candidate = recruitCandidateMapper.selectById(dbCandidate.getId());
        assertEquals(reqVO.getStatus(), candidate.getStatus());
        assertNotNull(candidate.getStatusUpdateTime());
    }

    @Test
    public void testUpdateRecruitCandidateStatus_cannotJumpToInterview() {
        // mock 数据
        HrmRecruitCandidateDO candidate = randomRecruitCandidateDO();
        recruitCandidateMapper.insert(candidate);
        HrmRecruitCandidateUpdateStatusReqVO reqVO = new HrmRecruitCandidateUpdateStatusReqVO()
                .setId(candidate.getId()).setStatus(HrmRecruitCandidateStatusEnum.INTERVIEW.getStatus());

        // 调用、断言
        assertServiceException(() -> recruitCandidateService.updateRecruitCandidateStatus(reqVO),
                RECRUIT_CANDIDATE_STATUS_TRANSITION_INVALID,
                HrmRecruitCandidateStatusEnum.INTERVIEW.getName());
        assertEquals(HrmRecruitCandidateStatusEnum.NEW.getStatus(),
                recruitCandidateMapper.selectById(candidate.getId()).getStatus());
    }

    @Test
    public void testUpdateRecruitCandidateStatus_sameJoinedStatus() {
        // mock 数据
        LocalDateTime entryTime = LocalDateTime.of(2026, 6, 1, 9, 0);
        LocalDateTime statusUpdateTime = LocalDateTime.of(2026, 7, 1, 10, 0);
        HrmRecruitCandidateDO candidate = randomRecruitCandidateDO(o -> o
                .setStatus(HrmRecruitCandidateStatusEnum.JOINED.getStatus())
                .setEntryTime(entryTime).setStatusUpdateTime(statusUpdateTime));
        recruitCandidateMapper.insert(candidate);
        HrmRecruitCandidateUpdateStatusReqVO reqVO = new HrmRecruitCandidateUpdateStatusReqVO()
                .setId(candidate.getId()).setStatus(HrmRecruitCandidateStatusEnum.JOINED.getStatus());

        // 调用
        recruitCandidateService.updateRecruitCandidateStatus(reqVO);

        // 断言
        HrmRecruitCandidateDO dbCandidate = recruitCandidateMapper.selectById(candidate.getId());
        assertEquals(entryTime, dbCandidate.getEntryTime());
        assertEquals(statusUpdateTime, dbCandidate.getStatusUpdateTime());
    }

    @Test
    public void testConfirmRecruitCandidateEntry_success() {
        // mock 数据
        HrmRecruitCandidateDO candidate = randomRecruitCandidateDO(o -> o
                .setStatus(HrmRecruitCandidateStatusEnum.PENDING_ENTRY.getStatus()));
        recruitCandidateMapper.insert(candidate);
        LocalDateTime entryTime = LocalDateTime.now().minusMinutes(1);

        // 调用
        recruitCandidateService.confirmRecruitCandidateEntry(candidate.getId(), entryTime);

        // 断言
        HrmRecruitCandidateDO dbCandidate = recruitCandidateMapper.selectById(candidate.getId());
        assertEquals(HrmRecruitCandidateStatusEnum.JOINED.getStatus(), dbCandidate.getStatus());
        assertEquals(entryTime, dbCandidate.getEntryTime());
        assertNotNull(dbCandidate.getStatusUpdateTime());
    }

    @Test
    public void testConfirmRecruitCandidateEntry_statusInvalid() {
        // mock 数据
        HrmRecruitCandidateDO candidate = randomRecruitCandidateDO(o -> o
                .setStatus(HrmRecruitCandidateStatusEnum.OFFER_SENT.getStatus()));
        recruitCandidateMapper.insert(candidate);

        // 调用、断言
        assertServiceException(() -> recruitCandidateService.confirmRecruitCandidateEntry(
                        candidate.getId(), LocalDateTime.now()),
                RECRUIT_CANDIDATE_STATUS_TRANSITION_INVALID,
                HrmRecruitCandidateStatusEnum.JOINED.getName());
        assertEquals(HrmRecruitCandidateStatusEnum.OFFER_SENT.getStatus(),
                recruitCandidateMapper.selectById(candidate.getId()).getStatus());
    }

    @Test
    public void testUpdateRecruitCandidatePost_success() {
        // mock 数据
        HrmRecruitCandidateDO dbCandidate = randomRecruitCandidateDO();
        recruitCandidateMapper.insert(dbCandidate);
        HrmRecruitPostDO recruitPost = randomRecruitPostDO();
        // 准备参数
        HrmRecruitCandidateUpdatePostReqVO reqVO = randomPojo(HrmRecruitCandidateUpdatePostReqVO.class, o -> o
                .setId(dbCandidate.getId()).setPostId(recruitPost.getId()));
        // mock 方法
        when(recruitPostService.validateRecruitPostExists(eq(reqVO.getPostId()))).thenReturn(recruitPost);

        // 调用
        recruitCandidateService.updateRecruitCandidatePost(reqVO);

        // 断言
        assertEquals(reqVO.getPostId(), recruitCandidateMapper.selectById(dbCandidate.getId()).getPostId());
    }

    @Test
    public void testUpdateRecruitCandidateChannel_success() {
        // mock 数据
        HrmRecruitCandidateDO dbCandidate = randomRecruitCandidateDO();
        recruitCandidateMapper.insert(dbCandidate);
        HrmRecruitChannelDO recruitChannel = randomPojo(HrmRecruitChannelDO.class);
        // 准备参数
        HrmRecruitCandidateUpdateChannelReqVO reqVO = randomPojo(HrmRecruitCandidateUpdateChannelReqVO.class, o -> o
                .setId(dbCandidate.getId()).setChannelId(recruitChannel.getId()));
        // mock 方法
        when(recruitChannelService.validateRecruitChannelExists(eq(reqVO.getChannelId()))).thenReturn(recruitChannel);

        // 调用
        recruitCandidateService.updateRecruitCandidateChannel(reqVO);

        // 断言
        assertEquals(reqVO.getChannelId(), recruitCandidateMapper.selectById(dbCandidate.getId()).getChannelId());
    }

    @Test
    public void testUpdateRecruitCandidateChannelByChannelId_success() {
        // mock 数据
        HrmRecruitCandidateDO firstCandidate = randomRecruitCandidateDO(o -> o.setChannelId(10L));
        recruitCandidateMapper.insert(firstCandidate);
        HrmRecruitCandidateDO secondCandidate = randomRecruitCandidateDO(o -> o.setChannelId(10L));
        recruitCandidateMapper.insert(secondCandidate);
        // 测试招聘渠道不匹配
        HrmRecruitCandidateDO retainedCandidate = randomRecruitCandidateDO(o -> o.setChannelId(30L));
        recruitCandidateMapper.insert(retainedCandidate);

        // 调用
        recruitCandidateService.updateRecruitCandidateChannelByChannelId(10L, 20L);

        // 断言
        assertEquals(20L, recruitCandidateMapper.selectById(firstCandidate.getId()).getChannelId());
        assertEquals(20L, recruitCandidateMapper.selectById(secondCandidate.getId()).getChannelId());
        assertEquals(30L, recruitCandidateMapper.selectById(retainedCandidate.getId()).getChannelId());
    }

    @Test
    public void testEliminateRecruitCandidate_success() {
        // mock 数据
        HrmRecruitCandidateDO dbCandidate = randomRecruitCandidateDO(o -> o
                .setStatus(HrmRecruitCandidateStatusEnum.INTERVIEW.getStatus()));
        recruitCandidateMapper.insert(dbCandidate);
        HrmEmployeeDO employee = randomPojo(HrmEmployeeDO.class);
        when(employeeService.getEmployeeByCandidateId(eq(dbCandidate.getId()))).thenReturn(employee);
        // 准备参数
        HrmRecruitCandidateUpdateEliminateReqVO reqVO = randomPojo(HrmRecruitCandidateUpdateEliminateReqVO.class,
                o -> o.setId(dbCandidate.getId()));

        // 调用
        recruitCandidateService.eliminateRecruitCandidate(reqVO);

        // 断言
        HrmRecruitCandidateDO candidate = recruitCandidateMapper.selectById(dbCandidate.getId());
        assertEquals(HrmRecruitCandidateStatusEnum.ELIMINATED.getStatus(), candidate.getStatus());
        assertEquals(reqVO.getEliminate(), candidate.getEliminate());
        assertEquals(reqVO.getRemark(), candidate.getRemark());
        verify(recruitInterviewService).finishCurrentRecruitInterviewForElimination(
                dbCandidate.getId(), reqVO.getEliminate());
        verify(employeeService).deleteEmployee(employee.getId());
    }

    @Test
    public void testEliminateRecruitCandidate_joinedStatusInvalid() {
        // mock 数据
        HrmRecruitCandidateDO candidate = randomRecruitCandidateDO(o -> o
                .setStatus(HrmRecruitCandidateStatusEnum.JOINED.getStatus()));
        recruitCandidateMapper.insert(candidate);
        HrmRecruitCandidateUpdateEliminateReqVO reqVO = randomPojo(
                HrmRecruitCandidateUpdateEliminateReqVO.class, o -> o.setId(candidate.getId()));

        // 调用、断言
        assertServiceException(() -> recruitCandidateService.eliminateRecruitCandidate(reqVO),
                RECRUIT_CANDIDATE_ELIMINATE_STATUS_INVALID);
        verifyNoInteractions(recruitInterviewService, employeeService);
    }

    @Test
    public void testConvertRecruitCandidateToEmployee_success() {
        // mock 数据
        HrmRecruitPostDO recruitPost = randomRecruitPostDO(o -> o
                .setPostName("Java 开发工程师").setDeptId(100L).setAreaId(310100));
        HrmRecruitCandidateDO dbCandidate = randomRecruitCandidateDO(o -> o
                .setName("张三").setPostId(recruitPost.getId()).setChannelId(10L)
                .setStatus(HrmRecruitCandidateStatusEnum.OFFER_SENT.getStatus()));
        recruitCandidateMapper.insert(dbCandidate);
        // 准备参数
        HrmRecruitCandidateEntryReqVO reqVO = randomRecruitCandidateEntryReqVO(dbCandidate.getId());
        reqVO.setChannelId(20L);
        Long employeeId = randomLongId();
        // mock 方法
        when(recruitPostService.validateRecruitPostExists(eq(recruitPost.getId()))).thenReturn(recruitPost);
        when(employeeService.createEmployee(any())).thenReturn(employeeId);

        // 调用
        Long result = recruitCandidateService.convertRecruitCandidateToEmployee(reqVO);

        // 断言
        assertEquals(employeeId, result);
        ArgumentCaptor<HrmEmployeeSaveReqVO> employeeCaptor = ArgumentCaptor.forClass(HrmEmployeeSaveReqVO.class);
        verify(employeeService).createEmployee(employeeCaptor.capture());
        HrmEmployeeSaveReqVO employeeReqVO = employeeCaptor.getValue();
        assertEquals(reqVO.getName(), employeeReqVO.getName());
        assertEquals(reqVO.getJobNumber(), employeeReqVO.getJobNumber());
        assertEquals(reqVO.getEntryTime(), employeeReqVO.getEntryTime());
        assertEquals(reqVO.getCompanyAgeStartTime(), employeeReqVO.getCompanyAgeStartTime());
        assertEquals(reqVO.getDeptId(), employeeReqVO.getDeptId());
        assertEquals(reqVO.getPostName(), employeeReqVO.getPostName());
        assertEquals(reqVO.getWorkCity(), employeeReqVO.getWorkCity());
        assertEquals(dbCandidate.getChannelId(), employeeReqVO.getChannelId());
        assertEquals(dbCandidate.getId(), employeeReqVO.getCandidateId());
        assertNull(employeeReqVO.getId());
        HrmRecruitCandidateDO candidate = recruitCandidateMapper.selectById(dbCandidate.getId());
        assertEquals(HrmRecruitCandidateStatusEnum.PENDING_ENTRY.getStatus(), candidate.getStatus());
        assertEquals(reqVO.getEntryTime(), candidate.getEntryTime());
    }

    @Test
    public void testConvertRecruitCandidateToEmployee_converted() {
        // mock 数据
        HrmRecruitCandidateDO dbCandidate = randomRecruitCandidateDO(o -> o
                .setStatus(HrmRecruitCandidateStatusEnum.OFFER_SENT.getStatus()));
        recruitCandidateMapper.insert(dbCandidate);
        when(employeeService.getEmployeeByCandidateId(eq(dbCandidate.getId())))
                .thenReturn(randomPojo(HrmEmployeeDO.class));
        // 准备参数
        HrmRecruitCandidateEntryReqVO reqVO = randomRecruitCandidateEntryReqVO(dbCandidate.getId());

        // 调用，并断言异常
        assertServiceException(() -> recruitCandidateService.convertRecruitCandidateToEmployee(reqVO),
                EMPLOYEE_CANDIDATE_CONVERTED);
        verify(employeeService, never()).createEmployee(any());
        verifyNoInteractions(recruitPostService);
    }

    @Test
    public void testConvertRecruitCandidateToEmployee_statusInvalid() {
        // mock 数据
        HrmRecruitCandidateDO candidate = randomRecruitCandidateDO(o -> o
                .setStatus(HrmRecruitCandidateStatusEnum.NEW.getStatus()));
        recruitCandidateMapper.insert(candidate);
        HrmRecruitCandidateEntryReqVO reqVO = randomRecruitCandidateEntryReqVO(candidate.getId());

        // 调用、断言
        assertServiceException(() -> recruitCandidateService.convertRecruitCandidateToEmployee(reqVO),
                RECRUIT_CANDIDATE_CONVERT_STATUS_INVALID);
        verifyNoInteractions(employeeService, recruitPostService);
    }

    @Test
    public void testGetRecruitCandidateList() {
        // mock 数据
        LocalDateTime now = LocalDateTime.of(2026, 7, 17, 12, 0);
        HrmRecruitCandidateDO expiredCandidate = randomRecruitCandidateDO(o -> o
                .setStatus(HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus())
                .setStatusUpdateTime(now.minusDays(5)));
        recruitCandidateMapper.insert(expiredCandidate);
        // 测试状态时间不匹配
        recruitCandidateMapper.insert(cloneIgnoreId(expiredCandidate,
                o -> o.setStatusUpdateTime(now.minusDays(1))));
        // 测试候选人状态不匹配
        recruitCandidateMapper.insert(cloneIgnoreId(expiredCandidate,
                o -> o.setStatus(HrmRecruitCandidateStatusEnum.NEW.getStatus())));
        // 准备参数
        List<Integer> statuses = singletonList(HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus());
        LocalDateTime statusUpdateTime = now.minusDays(3);

        // 调用
        List<HrmRecruitCandidateDO> candidates = recruitCandidateService.getRecruitCandidateList(
                statuses, statusUpdateTime);

        // 断言
        assertEquals(1, candidates.size());
        assertPojoEquals(expiredCandidate, candidates.get(0));
    }

    @Test
    public void testGetRecruitCandidateList_emptyStatuses() {
        // mock 数据
        recruitCandidateMapper.insert(randomRecruitCandidateDO());

        // 调用
        List<HrmRecruitCandidateDO> candidates = recruitCandidateService.getRecruitCandidateList(
                emptyList(), randomLocalDateTime());

        // 断言
        assertTrue(candidates.isEmpty());
    }

    @Test
    public void testGetJoinedCandidateCountMap() {
        // mock 数据
        Long javaPostId = randomLongId();
        Long productPostId = randomLongId();
        Long testPostId = randomLongId();
        recruitCandidateMapper.insert(randomRecruitCandidateDO(o -> o
                .setPostId(javaPostId).setStatus(HrmRecruitCandidateStatusEnum.JOINED.getStatus())));
        recruitCandidateMapper.insert(randomRecruitCandidateDO(o -> o
                .setPostId(javaPostId).setStatus(HrmRecruitCandidateStatusEnum.JOINED.getStatus())));
        // 测试候选人状态不匹配
        recruitCandidateMapper.insert(randomRecruitCandidateDO(o -> o
                .setPostId(javaPostId).setStatus(HrmRecruitCandidateStatusEnum.PENDING_ENTRY.getStatus())));
        recruitCandidateMapper.insert(randomRecruitCandidateDO(o -> o
                .setPostId(productPostId).setStatus(HrmRecruitCandidateStatusEnum.JOINED.getStatus())));

        // 调用
        Map<Long, Long> countMap = recruitCandidateService.getJoinedCandidateCountMap(
                asList(javaPostId, productPostId, testPostId));

        // 断言
        assertEquals(2L, countMap.get(javaPostId));
        assertEquals(1L, countMap.get(productPostId));
        assertNull(countMap.get(testPostId));
    }

    @Test
    public void testGetJoinedCandidateCountMap_emptyPostIds() {
        // mock 数据
        recruitCandidateMapper.insert(randomRecruitCandidateDO(o -> o
                .setStatus(HrmRecruitCandidateStatusEnum.JOINED.getStatus())));

        // 调用
        Map<Long, Long> countMap = recruitCandidateService.getJoinedCandidateCountMap(emptyList());

        // 断言
        assertTrue(countMap.isEmpty());
    }

    @Test
    public void testGetRecruitCandidateStatusCount() {
        // mock 职位数据
        HrmRecruitPostDO targetPost = randomRecruitPostDO(o -> o.setOwnerEmployeeId(100L));
        recruitPostMapper.insert(targetPost);
        HrmRecruitPostDO otherOwnerPost = cloneIgnoreId(targetPost, o -> o.setOwnerEmployeeId(200L));
        recruitPostMapper.insert(otherOwnerPost);
        // mock 候选人数据
        Long targetChannelId = randomLongId();
        Long otherChannelId = randomLongId();
        HrmRecruitCandidateDO targetCandidate = randomRecruitCandidateDO(o -> o
                .setPostId(targetPost.getId()).setChannelId(targetChannelId)
                .setStatus(HrmRecruitCandidateStatusEnum.NEW.getStatus()));
        recruitCandidateMapper.insert(targetCandidate);
        recruitCandidateMapper.insert(cloneIgnoreId(targetCandidate, o -> {}));
        recruitCandidateMapper.insert(cloneIgnoreId(targetCandidate,
                o -> o.setStatus(HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus())));
        // 测试招聘渠道不匹配
        recruitCandidateMapper.insert(cloneIgnoreId(targetCandidate,
                o -> o.setChannelId(otherChannelId).setStatus(HrmRecruitCandidateStatusEnum.JOINED.getStatus())));
        // 测试招聘负责人不匹配
        recruitCandidateMapper.insert(cloneIgnoreId(targetCandidate, o -> o
                .setPostId(otherOwnerPost.getId()).setStatus(HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus())));
        // 准备参数，状态条件不影响分组统计
        HrmRecruitCandidatePageReqVO reqVO = new HrmRecruitCandidatePageReqVO()
                .setChannelId(targetChannelId).setOwnerEmployeeId(100L)
                .setStatus(HrmRecruitCandidateStatusEnum.NEW.getStatus());

        // 调用
        Map<Integer, Long> countMap = recruitCandidateService.getRecruitCandidateStatusCount(reqVO);

        // 断言
        assertEquals(2L, countMap.get(HrmRecruitCandidateStatusEnum.NEW.getStatus()));
        assertEquals(1L, countMap.get(HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus()));
        assertNull(countMap.get(HrmRecruitCandidateStatusEnum.JOINED.getStatus()));
    }

    @Test
    public void testGetRecruitCandidateStatusCount_interviewDistinct() {
        // mock 数据
        LocalDateTime interviewTime = LocalDateTime.of(2026, 7, 17, 10, 0);
        HrmRecruitCandidateDO newCandidate = randomRecruitCandidateDO(o -> o
                .setStageNumber(1).setStatus(HrmRecruitCandidateStatusEnum.NEW.getStatus()));
        recruitCandidateMapper.insert(newCandidate);
        recruitInterviewMapper.insert(randomRecruitInterviewDO(o -> o
                .setCandidateId(newCandidate.getId()).setStageNumber(1)
                .setInterviewEmployeeId(10L).setInterviewTime(interviewTime)));
        recruitInterviewMapper.insert(randomRecruitInterviewDO(o -> o
                .setCandidateId(newCandidate.getId()).setStageNumber(1)
                .setInterviewEmployeeId(10L).setInterviewTime(interviewTime.plusHours(1))
                .setResult(HrmRecruitInterviewResultEnum.PASS.getResult())));
        HrmRecruitCandidateDO passedCandidate = randomRecruitCandidateDO(o -> o
                .setStageNumber(1).setStatus(HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus()));
        recruitCandidateMapper.insert(passedCandidate);
        recruitInterviewMapper.insert(randomRecruitInterviewDO(o -> o
                .setCandidateId(passedCandidate.getId()).setStageNumber(1)
                .setInterviewEmployeeId(10L).setInterviewTime(interviewTime)));
        // 准备参数
        HrmRecruitCandidatePageReqVO reqVO = new HrmRecruitCandidatePageReqVO()
                .setInterviewEmployeeId(10L)
                .setInterviewTime(new LocalDateTime[]{interviewTime.minusDays(1), interviewTime.plusDays(1)});

        // 调用
        Map<Integer, Long> countMap = recruitCandidateService.getRecruitCandidateStatusCount(reqVO);

        // 断言
        assertEquals(1L, countMap.get(HrmRecruitCandidateStatusEnum.NEW.getStatus()));
        assertEquals(1L, countMap.get(HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus()));
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static HrmRecruitCandidateDO randomRecruitCandidateDO(Consumer<HrmRecruitCandidateDO>... consumers) {
        Consumer<HrmRecruitCandidateDO> consumer = o -> o
                .setName(randomString()).setMobile(randomMobile()).setSex(1).setAge(28).setEmail(randomEmail())
                .setStageNumber(0).setWorkTime(5).setEducation(5)
                .setStatus(HrmRecruitCandidateStatusEnum.NEW.getStatus())
                .setEliminate(null).setStatusUpdateTime(randomLocalDateTime()).setEntryTime(null)
                .setResumeUrls(singletonList(randomURL()));
        return randomPojo(HrmRecruitCandidateDO.class, ArrayUtils.append(consumer, consumers));
    }

    @SafeVarargs
    private static HrmRecruitPostDO randomRecruitPostDO(Consumer<HrmRecruitPostDO>... consumers) {
        Consumer<HrmRecruitPostDO> consumer = o -> o
                .setPostName(randomString()).setDeptId(randomLongId()).setAreaId(310100).setRecruitNum(1)
                .setJobNature(1).setWorkTime(1).setEducationRequire(1)
                .setMinSalary(new BigDecimal("10000.00")).setMaxSalary(new BigDecimal("20000.00")).setSalaryUnit(2)
                .setMinAge(22).setMaxAge(35).setEmergencyLevel(2)
                .setOwnerEmployeeId(null).setInterviewEmployeeIds(emptyList())
                .setStatus(HrmRecruitPostStatusEnum.RECRUITING.getStatus()).setStopReason(null);
        return randomPojo(HrmRecruitPostDO.class, ArrayUtils.append(consumer, consumers));
    }

    @SafeVarargs
    private static HrmRecruitInterviewDO randomRecruitInterviewDO(Consumer<HrmRecruitInterviewDO>... consumers) {
        Consumer<HrmRecruitInterviewDO> consumer = o -> o.setType(1).setStageNumber(1)
                .setOtherInterviewEmployeeIds(emptyList())
                .setResult(HrmRecruitInterviewResultEnum.UNFINISHED.getResult());
        return randomPojo(HrmRecruitInterviewDO.class, ArrayUtils.append(consumer, consumers));
    }

    @SafeVarargs
    private static HrmRecruitCandidateSaveReqVO randomRecruitCandidateSaveReqVO(
            Consumer<HrmRecruitCandidateSaveReqVO>... consumers) {
        Consumer<HrmRecruitCandidateSaveReqVO> consumer = o -> o.setId(null)
                .setName(randomString()).setMobile(randomMobile()).setSex(1).setAge(28).setEmail(randomEmail())
                .setPostId(randomLongId()).setWorkTime(5).setEducation(5)
                .setChannelId(randomLongId()).setResumeUrls(singletonList(randomURL()));
        return randomPojo(HrmRecruitCandidateSaveReqVO.class, ArrayUtils.append(consumer, consumers));
    }

    private static HrmRecruitCandidateEntryReqVO randomRecruitCandidateEntryReqVO(Long candidateId) {
        return randomPojo(HrmRecruitCandidateEntryReqVO.class, o -> o
                .setCandidateId(candidateId).setId(randomLongId()).setName("张三").setMobile(randomMobile())
                .setJobNumber(null).setEntryTime(LocalDateTime.of(2026, 8, 1, 9, 30))
                .setCompanyAgeStartTime(LocalDateTime.of(2026, 8, 1, 9, 30))
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus())
                .setStatus(2).setType(1).setProbation(6)
                .setDeptId(100L).setPostName("Java 开发工程师").setWorkCity("上海市 上海市")
                .setChannelId(10L));
    }

}
