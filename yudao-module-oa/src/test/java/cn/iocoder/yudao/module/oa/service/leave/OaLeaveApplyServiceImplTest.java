package cn.iocoder.yudao.module.oa.service.leave;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.oa.controller.admin.leave.vo.OaLeaveApplyPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.leave.vo.OaLeaveApplySaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.leave.vo.OaLeaveApplySubmitReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.leave.OaLeaveApplyDO;
import cn.iocoder.yudao.module.oa.dal.mysql.leave.OaLeaveApplyMapper;
import cn.iocoder.yudao.module.oa.enums.attendance.OaAttendanceTypeEnum;
import cn.iocoder.yudao.module.oa.enums.leave.OaLeaveTypeEnum;
import cn.iocoder.yudao.module.oa.service.attendance.OaAttendanceService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getMonthDateTimeRange;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaLeaveApplyServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaLeaveApplyServiceImpl.class)
public class OaLeaveApplyServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaLeaveApplyService leaveApplyService;

    @Resource
    private OaLeaveApplyMapper leaveApplyMapper;

    @MockitoBean
    private OaAttendanceService attendanceService;
    @MockitoBean
    private BpmProcessInstanceApi processInstanceApi;

    @BeforeEach
    public void before() {
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(10L).setUserType(2), new MockHttpServletRequest());
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testUpdateLeaveApplyStatus_createAttendance() {
        // mock 数据
        OaLeaveApplyDO apply = randomPojo(OaLeaveApplyDO.class, row -> row.setId(null).setType(1).setUrgency(1))
                .setStatus(1).setDays(2).setStartTime(LocalDateTime.of(2026, 9, 1, 8, 0));
        apply.setCreator("10");
        leaveApplyMapper.insert(apply);

        // 调用：审批通过后生成考勤明细
        leaveApplyService.updateLeaveApplyStatus(apply.getId(), 2);
        // 断言
        assertEquals(2, leaveApplyMapper.selectById(apply.getId()).getStatus());
        verify(attendanceService).createApplyAttendance(10L,
                OaAttendanceTypeEnum.LEAVE,
                apply.getStartTime());
    }

    @Test
    public void testUpdateLeaveApplyStatus_rejectWithoutAttendance() {
        // mock 数据
        OaLeaveApplyDO apply = randomPojo(OaLeaveApplyDO.class, row -> row.setId(null).setType(1).setUrgency(1)).setStatus(1);
        leaveApplyMapper.insert(apply);

        // 调用
        leaveApplyService.updateLeaveApplyStatus(apply.getId(), 3);
        // 断言
        verifyNoInteractions(attendanceService);
    }

    @Test
    public void testDraft_updateAndSubmitPermissions() {
        // mock 数据
        Long id = leaveApplyService.createLeaveApply(randomLeaveApplySaveReqVO());
        OaLeaveApplySaveReqVO reqVO = randomLeaveApplySaveReqVO().setId(id).setTitle("修改后的草稿");

        // 调用，并断言：他人不能修改、提交
        assertServiceException(() -> leaveApplyService.updateLeaveApply(reqVO, 99L), APPLY_ACCESS_DENIED);
        assertServiceException(() -> leaveApplyService.submitLeaveApply(new OaLeaveApplySubmitReqVO()
                .setId(id), 99L), APPLY_ACCESS_DENIED);
        leaveApplyService.updateLeaveApply(reqVO, 10L);
        assertEquals("修改后的草稿", leaveApplyMapper.selectById(id).getTitle());
        assertEquals(-1, leaveApplyMapper.selectById(id).getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testSubmit_duplicateAndUpdateRunning() {
        // mock 数据
        Long id = leaveApplyService.createLeaveApply(randomLeaveApplySaveReqVO());
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("process-1");

        // 调用
        leaveApplyService.submitLeaveApply(new OaLeaveApplySubmitReqVO().setId(id), 10L);
        // 断言
        assertServiceException(() -> leaveApplyService.submitLeaveApply(new OaLeaveApplySubmitReqVO()
                .setId(id), 10L), APPLY_STATUS_INVALID);
        assertServiceException(() -> leaveApplyService.updateLeaveApply(randomLeaveApplySaveReqVO()
                .setId(id), 10L), APPLY_STATUS_INVALID);
        verify(processInstanceApi, times(1)).createProcessInstance(eq(10L), any());
    }

    @Test
    public void testCreateLeaveApply_success() {
        // 准备参数
        OaLeaveApplySaveReqVO reqVO = randomLeaveApplySaveReqVO();
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("process-1");

        // 调用
        Long id = leaveApplyService.createLeaveApply(reqVO);
        assertEquals(-1, leaveApplyMapper.selectById(id).getStatus());
        assertNull(leaveApplyMapper.selectById(id).getProcessInstanceId());
        verifyNoInteractions(processInstanceApi);
        leaveApplyService.submitLeaveApply(new OaLeaveApplySubmitReqVO().setId(id), 10L);

        // 断言
        OaLeaveApplyDO application = leaveApplyMapper.selectById(id);
        assertEquals("10", application.getCreator());
        assertEquals(1, application.getStatus());
        assertEquals("process-1", application.getProcessInstanceId());
        assertEquals(1, application.getDays());
        verify(processInstanceApi).createProcessInstance(eq(10L), argThat(dto ->
                "oa_leave_apply".equals(dto.getProcessDefinitionKey()) && id.toString().equals(dto.getBusinessKey())
                        && !dto.getVariables().containsKey("title") && Integer.valueOf(1).equals(dto.getVariables().get("days"))));
    }

    @Test
    public void testCreateLeaveApply_processFailureRollback() {
        // 准备参数
        OaLeaveApplySaveReqVO reqVO = randomLeaveApplySaveReqVO();
        // mock 方法
        when(processInstanceApi.createProcessInstance(anyLong(), any())).thenThrow(new IllegalStateException("流程尚未配置"));

        // 调用
        Long id = leaveApplyService.createLeaveApply(reqVO);
        // 断言
        assertThrows(IllegalStateException.class, () -> leaveApplyService.submitLeaveApply(
                new OaLeaveApplySubmitReqVO().setId(id), 10L));
        assertEquals(1L, leaveApplyMapper.selectCount());
        assertEquals(-1, leaveApplyMapper.selectById(id).getStatus());
        assertNull(leaveApplyMapper.selectById(id).getProcessInstanceId());
    }

    @Test
    public void testGetLeaveApply_success() {
        // mock 数据
        OaLeaveApplyDO application = randomLeaveApplyDO();
        leaveApplyMapper.insert(application);

        // 调用
        OaLeaveApplyDO result = leaveApplyService.getLeaveApply(application.getId());

        // 断言
        assertEquals(application.getId(), result.getId());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testGetLeaveApply_notExists() {

        // 调用
        OaLeaveApplyDO result = leaveApplyService.getLeaveApply(1024L);

        // 断言
        assertNull(result);
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testSubmitLeaveApply_synchronousApproval() {
        // mock 数据
        Long id = leaveApplyService.createLeaveApply(randomLeaveApplySaveReqVO());
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenAnswer(invocation -> {
            BpmProcessInstanceCreateReqDTO reqDTO = invocation.getArgument(1);
            leaveApplyService.updateLeaveApplyStatus(Long.valueOf(reqDTO.getBusinessKey()), 2);
            return "process-1";
        });

        // 调用
        leaveApplyService.submitLeaveApply(new OaLeaveApplySubmitReqVO().setId(id), 10L);

        // 断言
        OaLeaveApplyDO leaveApply = leaveApplyMapper.selectById(id);
        assertEquals(2, leaveApply.getStatus());
        assertEquals("process-1", leaveApply.getProcessInstanceId());
    }

    @Test
    public void testGetLeaveApplyPage_scopeAndFilter() {
        // mock 数据
        OaLeaveApplyDO application = randomLeaveApplyDO().setTitle("匹配申请");
        leaveApplyMapper.insert(application);
        OaLeaveApplyDO otherApplication = randomLeaveApplyDO().setTitle("匹配申请");
        otherApplication.setCreator("99");
        leaveApplyMapper.insert(otherApplication);
        leaveApplyMapper.insert(randomLeaveApplyDO().setId(null).setStatus(2).setTitle("匹配申请"));
        // 准备参数
        OaLeaveApplyPageReqVO reqVO = new OaLeaveApplyPageReqVO().setTitle("匹配").setStatus(1);

        // 调用
        PageResult<OaLeaveApplyDO> page = leaveApplyService.getLeaveApplyPage(10L, reqVO);

        // 断言
        assertEquals(1L, page.getTotal());
        assertEquals(application.getId(), CollUtil.getFirst(page.getList()).getId());
    }

    @Test
    public void testUpdateLeaveApplyStatus_successAndRepeated() {
        // mock 数据
        OaLeaveApplyDO application = randomLeaveApplyDO();
        leaveApplyMapper.insert(application);

        // 调用
        leaveApplyService.updateLeaveApplyStatus(application.getId(), 2);
        leaveApplyService.updateLeaveApplyStatus(application.getId(), 2);

        // 断言
        assertEquals(2, leaveApplyMapper.selectById(application.getId()).getStatus());
    }

    @Test
    public void testUpdateLeaveApplyStatus_notExists() {

        // 调用，并断言异常
        assertServiceException(() -> leaveApplyService.updateLeaveApplyStatus(1024L, 2), APPLY_NOT_EXISTS);
    }

    @Test
    public void testCreateLeaveApply_crossMidnight() {
        // 准备参数
        OaLeaveApplySaveReqVO reqVO = randomLeaveApplySaveReqVO()
                .setStartTime(LocalDateTime.of(2026, 9, 13, 23, 0))
                .setEndTime(LocalDateTime.of(2026, 9, 14, 1, 0));

        // 调用
        Long id = leaveApplyService.createLeaveApply(reqVO);

        // 断言：跨午夜的 2 小时仍按 1 天计算，不按首尾日期计为 2 天
        assertEquals(1, leaveApplyMapper.selectById(id).getDays());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testUpdateLeaveApply_recalculateDays() {
        // mock 数据
        Long id = leaveApplyService.createLeaveApply(randomLeaveApplySaveReqVO());
        // 准备参数
        OaLeaveApplySaveReqVO reqVO = randomLeaveApplySaveReqVO().setId(id)
                .setStartTime(LocalDateTime.of(2026, 9, 13, 8, 0))
                .setEndTime(LocalDateTime.of(2026, 9, 14, 20, 0));

        // 调用
        leaveApplyService.updateLeaveApply(reqVO, 10L);

        // 断言
        OaLeaveApplyDO leaveApply = leaveApplyMapper.selectById(id);
        assertEquals(2, leaveApply.getDays());
        assertEquals(-1, leaveApply.getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    @ParameterizedTest
    @CsvSource({"2,4", "4,10", "6,10"})
    public void testCreateLeaveApply_daysLimit(Integer type, Integer maxDays) {
        // 准备参数：恰好达到上限
        LocalDateTime startTime = LocalDateTime.of(2026, 9, 1, 9, 0);
        OaLeaveApplySaveReqVO reqVO = randomLeaveApplySaveReqVO().setType(type)
                .setStartTime(startTime).setEndTime(startTime.plusDays(maxDays));

        // 调用
        Long id = leaveApplyService.createLeaveApply(reqVO);

        // 断言：合法申请正常保存；多一分钟向上取整后超限
        assertEquals(maxDays, leaveApplyMapper.selectById(id).getDays());
        reqVO.setEndTime(reqVO.getEndTime().plusMinutes(1));
        assertServiceException(() -> leaveApplyService.createLeaveApply(reqVO), LEAVE_APPLY_DAYS_EXCEEDED,
                OaLeaveTypeEnum.valueOf(type).getName(), maxDays);
        assertEquals(1L, leaveApplyMapper.selectCount());
    }

    @ParameterizedTest
    @CsvSource({"1", "3", "5", "7"})
    public void testCreateLeaveApply_otherTypesNotLimited(Integer type) {
        // 准备参数：其他类型不套用单次天数上限
        LocalDateTime startTime = LocalDateTime.of(2026, 9, 1, 9, 0);
        OaLeaveApplySaveReqVO reqVO = randomLeaveApplySaveReqVO().setType(type)
                .setStartTime(startTime).setEndTime(startTime.plusDays(20));

        // 调用
        Long id = leaveApplyService.createLeaveApply(reqVO);

        // 断言
        assertEquals(20, leaveApplyMapper.selectById(id).getDays());
    }

    @Test
    public void testUpdateLeaveApply_daysLimit() {
        // mock 数据
        Long id = leaveApplyService.createLeaveApply(randomLeaveApplySaveReqVO());
        // 准备参数
        OaLeaveApplySaveReqVO reqVO = randomLeaveApplySaveReqVO().setId(id).setType(2);
        reqVO.setEndTime(reqVO.getStartTime().plusDays(5));

        // 调用，并断言：修改类型或时间不能绕过上限
        assertServiceException(() -> leaveApplyService.updateLeaveApply(reqVO, 10L),
                LEAVE_APPLY_DAYS_EXCEEDED, "事假", 4);
        assertEquals(1, leaveApplyMapper.selectById(id).getType());
        assertEquals(1, leaveApplyMapper.selectById(id).getDays());
    }

    @Test
    public void testSubmitLeaveApply_legacyDraftDaysLimit() {
        // mock 数据：旧草稿超出限制
        OaLeaveApplyDO application = randomLeaveApplyDO().setType(4).setDays(11).setStatus(-1);
        leaveApplyMapper.insert(application);
        // 准备参数
        OaLeaveApplySubmitReqVO reqVO = new OaLeaveApplySubmitReqVO().setId(application.getId());

        // 调用，并断言异常
        assertServiceException(() -> leaveApplyService.submitLeaveApply(reqVO, 10L),
                LEAVE_APPLY_DAYS_EXCEEDED, "婚假", 10);
        assertEquals(-1, leaveApplyMapper.selectById(application.getId()).getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testGetApprovedLeaveDaysMap() {
        // mock 数据：包含月初、月末开始且跨月的申请
        LocalDateTime startTime = LocalDateTime.of(2026, 9, 1, 0, 0);
        leaveApplyMapper.insert(randomLeaveApplyDO().setStatus(2).setStartTime(startTime).setDays(2));
        leaveApplyMapper.insert(randomLeaveApplyDO().setStatus(2).setStartTime(startTime.plusMonths(1).minusSeconds(1))
                .setEndTime(startTime.plusMonths(1).plusDays(2)).setDays(3));
        // 排除非通过、前后月份、其他用户和已删除的申请
        for (Integer status : Arrays.asList(-1, 1, 3, 4)) {
            leaveApplyMapper.insert(randomLeaveApplyDO().setStatus(status).setStartTime(startTime).setDays(10));
        }
        leaveApplyMapper.insert(randomLeaveApplyDO().setStatus(2).setStartTime(startTime.minusSeconds(1)).setDays(10));
        leaveApplyMapper.insert(randomLeaveApplyDO().setStatus(2).setStartTime(startTime.plusMonths(1)).setDays(10));
        OaLeaveApplyDO otherUserApply = randomLeaveApplyDO().setStatus(2).setStartTime(startTime).setDays(10);
        otherUserApply.setCreator("99");
        leaveApplyMapper.insert(otherUserApply);
        OaLeaveApplyDO deletedApply = randomLeaveApplyDO().setStatus(2).setStartTime(startTime).setDays(10);
        deletedApply.setDeleted(true);
        leaveApplyMapper.insert(deletedApply);
        // 准备参数
        LocalDateTime[] monthTime = getMonthDateTimeRange(2026, 9);

        // 调用
        Map<Long, Integer> result = leaveApplyService.getApprovedLeaveDaysMap(Arrays.asList(10L, 20L), monthTime);

        // 断言：整笔归入开始月份，无申请用户不返回
        assertEquals(Collections.singletonMap(10L, 5), result);
        assertTrue(leaveApplyService.getApprovedLeaveDaysMap(Collections.emptyList(), monthTime).isEmpty());
    }

    // ========== 随机对象 ==========

    /**
     * 构造申请参数，固定业务字段的合法取值。
     *
     * @return 请求参数
     */
    private static OaLeaveApplySaveReqVO randomLeaveApplySaveReqVO() {
        return randomPojo(OaLeaveApplySaveReqVO.class).setTitle("业务申请").setUrgency(1).setId(null).setType(1)
                .setStartTime(LocalDateTime.of(2026, 9, 13, 8, 0)).setEndTime(LocalDateTime.of(2026, 9, 13, 20, 0));
    }

    /**
     * 构造测试数据，固定业务字段的合法取值。
     *
     * @return 未入库的测试对象
     */
    private static OaLeaveApplyDO randomLeaveApplyDO() {
        return randomPojo(OaLeaveApplyDO.class, application -> {
            application.setId(null).setTitle("申请").setUrgency(1).setStatus(1).setProcessInstanceId("process-1");
            application.setCreator("10").setDeleted(false);
        });
    }

}
