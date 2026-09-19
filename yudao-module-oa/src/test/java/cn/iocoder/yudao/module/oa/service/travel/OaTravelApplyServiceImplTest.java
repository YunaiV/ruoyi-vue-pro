package cn.iocoder.yudao.module.oa.service.travel;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.oa.controller.admin.travel.vo.apply.*;
import cn.iocoder.yudao.module.oa.controller.admin.travel.vo.apply.OaTravelApplySubmitReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.travel.OaTravelApplyDO;
import cn.iocoder.yudao.module.oa.dal.mysql.travel.OaTravelApplyMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.oa.enums.attendance.OaAttendanceTypeEnum;
import cn.iocoder.yudao.module.oa.service.attendance.OaAttendanceService;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getMonthDateTimeRange;
import static cn.iocoder.yudao.framework.common.util.object.BeanUtils.toBean;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaTravelApplyServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({OaTravelApplyServiceImpl.class, ValidationAutoConfiguration.class})
public class OaTravelApplyServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaTravelApplyService travelApplyService;

    @Resource
    private OaTravelApplyMapper travelApplyMapper;

    @MockitoBean
    private OaAttendanceService attendanceService;
    @MockitoBean
    private OaNoRedisDAO noRedisDAO;
    @MockitoBean
    private AdminUserApi adminUserApi;
    @MockitoBean
    private DictDataApi dictDataApi;
    @MockitoBean
    private BpmProcessInstanceApi processInstanceApi;
    @BeforeEach
    public void before() {
        DictFrameworkUtils.init(dictDataApi);
        DictFrameworkUtils.clearCache();
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(10L).setUserType(2),
                new MockHttpServletRequest());
    }

    @AfterEach
    public void tearDown() {
        DictFrameworkUtils.clearCache();
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testValidateApprovedTravelApply_reimburseStatus() {
        // mock 数据
        OaTravelApplyDO apply = randomTravelApplyDO().setStatus(2).setReimburseStatus(false);
        apply.setCreator("10");
        travelApplyMapper.insert(apply);

        // 调用，并断言：未报销可关联，报销完成后不能再关联
        assertEquals(apply.getId(), travelApplyService.validateApprovedTravelApply(apply.getId(), 10L).getId());
        travelApplyMapper.updateById(new OaTravelApplyDO().setId(apply.getId()).setReimburseStatus(true));
        assertServiceException(() -> travelApplyService.validateApprovedTravelApply(apply.getId(), 10L),
                TRAVEL_APPLY_ALREADY_REIMBURSED);
    }

    @Test
    public void testUpdateTravelApplyStatus_createAttendance() {
        // mock 数据
        OaTravelApplyDO apply = randomTravelApplyDO()
                .setStatus(1).setDays(2).setStartTime(LocalDateTime.of(2026, 9, 1, 8, 0));
        apply.setCreator("10");
        travelApplyMapper.insert(apply);

        // 调用：审批通过后生成考勤明细
        travelApplyService.updateTravelApplyStatus(apply.getId(), 2);
        // 断言
        assertEquals(2, travelApplyMapper.selectById(apply.getId()).getStatus());
        verify(attendanceService).createApplyAttendance(10L,
                OaAttendanceTypeEnum.TRAVEL,
                apply.getStartTime());
    }

    @Test
    public void testUpdateTravelApplyStatus_rejectWithoutAttendance() {
        // mock 数据
        OaTravelApplyDO apply = randomTravelApplyDO().setStatus(1);
        travelApplyMapper.insert(apply);

        // 调用
        travelApplyService.updateTravelApplyStatus(apply.getId(), 3);
        // 断言
        verifyNoInteractions(attendanceService);
    }

    @Test
    public void testCreateTravelApply_duplicateNo() {
        // mock 数据
        OaTravelApplyDO record = randomTravelApplyDO();
        travelApplyMapper.insert(record);
        // mock 方法
        when(noRedisDAO.generate(anyString())).thenReturn(record.getNo());
        when(adminUserApi.validateUser(10L)).thenReturn(new AdminUserRespDTO().setId(10L).setDeptId(20L));

        // 调用，并断言异常
        assertServiceException(() -> travelApplyService.createTravelApply(toBean(randomTravelApplyDO(), OaTravelApplySaveReqVO.class), 10L),
                TRAVEL_NO_DUPLICATE);
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testSubmitTravelApply_savedContentAndAssignees() {
        // mock 数据
        OaTravelApplyDO record = randomTravelApplyDO().setDays(2);
        travelApplyMapper.insert(record);
        // 准备参数
        OaTravelApplySubmitReqVO reqVO = new OaTravelApplySubmitReqVO().setId(record.getId())
                .setStartUserSelectAssignees(Collections.singletonMap("approve", Collections.singletonList(20L)));
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("new-process");

        // 调用
        travelApplyService.submitTravelApply(reqVO, 10L);

        // 断言：仅提交审批，不覆盖正文及明细
        OaTravelApplyDO updated = travelApplyMapper.selectById(record.getId());
        assertEquals(record.getReason(), updated.getReason());
        assertEquals(record.getItems(), updated.getItems());
        assertEquals(record.getFileUrls(), updated.getFileUrls());
        verify(processInstanceApi).createProcessInstance(eq(10L), argThat(dto ->
                Integer.valueOf(2).equals(dto.getVariables().get("days"))
                        && reqVO.getStartUserSelectAssignees().equals(dto.getStartUserSelectAssignees())));
    }

    @Test
    public void testSubmitTravelApply_notOwner() {
        // mock 数据
        OaTravelApplyDO record = randomTravelApplyDO();
        travelApplyMapper.insert(record);
        // 准备参数
        OaTravelApplySubmitReqVO reqVO = new OaTravelApplySubmitReqVO().setId(record.getId());

        // 调用，并断言异常
        assertServiceException(() -> travelApplyService.submitTravelApply(reqVO, 20L), TRAVEL_ACCESS_DENIED);
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testCreateTravelApply_jsonAndDays() {
        // 准备参数
        OaTravelApplySaveReqVO reqVO = toBean(randomTravelApplyDO(), OaTravelApplySaveReqVO.class);
        // mock 方法
        when(noRedisDAO.generate(anyString())).thenReturn("TRAVEL-NEW");
        when(adminUserApi.validateUser(10L)).thenReturn(new AdminUserRespDTO().setId(10L).setDeptId(20L));

        // 调用
        Long id = travelApplyService.createTravelApply(reqVO, 10L);

        // 断言：内联转换保留明细、附件和天数计算
        OaTravelApplyDO record = travelApplyMapper.selectById(id);
        assertEquals(1, record.getDays());
        assertEquals(reqVO.getItems().size(), record.getItems().size());
        assertEquals(reqVO.getFileUrls(), record.getFileUrls());
        assertEquals("TRAVEL-NEW", record.getNo());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testUpdateTravelApply_notExists() {
        // 准备参数
        OaTravelApplySaveReqVO reqVO = toBean(randomTravelApplyDO(), OaTravelApplySaveReqVO.class).setId(999L);

        // 调用，并断言：修改不存在的单据不会转为新增
        assertServiceException(() -> travelApplyService.updateTravelApply(reqVO, 10L), TRAVEL_NOT_EXISTS);
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    @Test
    public void testSubmitTravelApply_unsaved() {
        // 准备参数
        OaTravelApplySubmitReqVO reqVO = new OaTravelApplySubmitReqVO().setId(999L);

        // 调用，并断言异常
        assertServiceException(() -> travelApplyService.submitTravelApply(reqVO, 10L), TRAVEL_NOT_EXISTS);
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    @Test
    public void testCreateTravelApply_nullItem() {
        // 准备参数
        OaTravelApplySaveReqVO reqVO = toBean(randomTravelApplyDO(), OaTravelApplySaveReqVO.class)
                .setItems(Collections.singletonList(null));

        // 调用，并断言：空明细由级联校验拦截，不进入字典查询及保存
        assertThrows(ConstraintViolationException.class, () -> travelApplyService.createTravelApply(reqVO, 10L));
        verifyNoInteractions(dictDataApi, noRedisDAO, processInstanceApi);
    }

    @Test
    public void testCreateTravelApply_invalidItemDates() {
        // 准备参数
        OaTravelApplySaveReqVO reqVO = toBean(randomTravelApplyDO(), OaTravelApplySaveReqVO.class)
                .setItems(Collections.singletonList(
                new OaTravelApplySaveReqVO.Item().setStartTime(LocalDateTime.of(2026, 9, 15, 0, 0))
                        .setEndTime(LocalDateTime.of(2026, 9, 14, 0, 0))));

        // 调用，并断言
        assertThrows(ConstraintViolationException.class, () -> travelApplyService.createTravelApply(reqVO, 10L));
        verifyNoInteractions(dictDataApi, noRedisDAO, processInstanceApi);
    }

    @Test
    public void testCreateTravelApply_emptyDraft() {
        // 准备参数
        OaTravelApplySaveReqVO reqVO = new OaTravelApplySaveReqVO();

        // 调用，并断言：草稿保存也必须填写完整内容
        assertThrows(ConstraintViolationException.class, () -> travelApplyService.createTravelApply(reqVO, 10L));
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    @Test
    public void testUpdateTravelApply_jsonAndDays() {
        // mock 数据
        OaTravelApplyDO record = randomTravelApplyDO();
        travelApplyMapper.insert(record);
        // 准备参数
        OaTravelApplySaveReqVO reqVO = toBean(record, OaTravelApplySaveReqVO.class)
                .setStartTime(LocalDateTime.of(2026, 12, 31, 0, 0))
                .setEndTime(LocalDateTime.of(2027, 1, 2, 0, 0))
                .setFileUrls(Collections.singletonList("https://example.com/receipt.pdf"));

        // 调用
        travelApplyService.updateTravelApply(reqVO, 10L);

        // 断言：跨年按实际时长计算天数，JSON 明细正常读取
        OaTravelApplyDO updated = travelApplyMapper.selectById(record.getId());
        assertEquals(2, updated.getDays());
        assertEquals(1, updated.getItems().size());
        assertEquals(reqVO.getFileUrls(), updated.getFileUrls());
        assertEquals(record.getNo(), updated.getNo());
    }

    @Test
    public void testUpdateTravelApply_missingRequiredFields() {
        // mock 数据
        OaTravelApplyDO record = randomTravelApplyDO();
        travelApplyMapper.insert(record);
        // 准备参数
        OaTravelApplySaveReqVO reqVO = toBean(record, OaTravelApplySaveReqVO.class).setReason(null);

        // 调用，并断言：修改不能清空必填内容，已有数据保持不变
        assertThrows(ConstraintViolationException.class, () -> travelApplyService.updateTravelApply(reqVO, 10L));
        OaTravelApplyDO updated = travelApplyMapper.selectById(record.getId());
        assertEquals(record.getReason(), updated.getReason());
        assertEquals(record.getStatus(), updated.getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testCreateTravelApply_invalidDates() {
        // 准备参数
        OaTravelApplySaveReqVO reqVO = toBean(randomTravelApplyDO(), OaTravelApplySaveReqVO.class).setId(null)
                .setStartTime(LocalDateTime.of(2026, 9, 15, 0, 0)).setEndTime(LocalDateTime.of(2026, 9, 14, 0, 0));

        // 调用，并断言
        assertThrows(ConstraintViolationException.class, () -> travelApplyService.createTravelApply(reqVO, 10L));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void testUpdateTravelApply_notEditable(Integer status) {
        // mock 数据
        OaTravelApplyDO record = randomTravelApplyDO().setStatus(status);
        travelApplyMapper.insert(record);
        // 准备参数
        OaTravelApplySaveReqVO reqVO = toBean(record, OaTravelApplySaveReqVO.class);

        // 调用，并断言异常
        assertServiceException(() -> travelApplyService.updateTravelApply(reqVO, 10L), TRAVEL_STATUS_INVALID);
    }

    @Test
    public void testCreateTravelApply_missingFields() {
        // 准备参数
        OaTravelApplySaveReqVO reqVO = toBean(randomTravelApplyDO().setReason(null)
                .setStartTime(null), OaTravelApplySaveReqVO.class);

        // 调用，并断言：必填校验在保存时完成，不生成单号或发起审批
        assertThrows(ConstraintViolationException.class, () -> travelApplyService.createTravelApply(reqVO, 10L));
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    @Test
    public void testCreateTravelApply_invalidEstimatedPrice() {
        // 准备参数
        OaTravelApplySaveReqVO reqVO = toBean(randomTravelApplyDO(), OaTravelApplySaveReqVO.class)
                .setEstimatedPrice(new BigDecimal("-1"));

        // 调用，并断言：保存入口拦截非法金额
        assertThrows(ConstraintViolationException.class, () -> travelApplyService.createTravelApply(reqVO, 10L));
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    @Test
    public void testCreateTravelApply_invalidTransportType() {
        // 准备参数
        OaTravelApplySaveReqVO reqVO = toBean(randomTravelApplyDO(), OaTravelApplySaveReqVO.class)
                .setItems(Collections.singletonList(
                new OaTravelApplySaveReqVO.Item().setTransportType(999)));
        // mock 方法
        when(dictDataApi.getDictDataList(anyString())).thenReturn(Collections.emptyList());

        // 调用，并断言：通过 InDict 校验交通方式，不再手工调用字典校验 API
        assertThrows(ConstraintViolationException.class, () -> travelApplyService.createTravelApply(reqVO, 10L));
        verify(dictDataApi, never()).validateDictDataList(anyString(), anyCollection());
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    @Test
    public void testCreateTravelApply_missingItems() {
        // 准备参数
        OaTravelApplySaveReqVO reqVO = toBean(randomTravelApplyDO()
                .setItems(Collections.emptyList()), OaTravelApplySaveReqVO.class);

        // 调用，并断言：必填校验在保存时完成，不生成单号或发起审批
        assertThrows(ConstraintViolationException.class, () -> travelApplyService.createTravelApply(reqVO, 10L));
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 3, 4})
    public void testSubmitTravelApply_resubmit(Integer status) {
        // mock 数据
        OaTravelApplyDO record = randomTravelApplyDO().setStatus(status).setProcessInstanceId("old-process");
        travelApplyMapper.insert(record);
        // 准备参数
        OaTravelApplySubmitReqVO reqVO = new OaTravelApplySubmitReqVO().setId(record.getId());
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("new-process");

        // 调用
        travelApplyService.submitTravelApply(reqVO, 10L);
        Long id = record.getId();

        // 断言：不重复创建单据，使用新的审批实例
        OaTravelApplyDO updated = travelApplyMapper.selectById(id);
        assertEquals(record.getId(), id);
        assertEquals(1, updated.getStatus());
        assertEquals("new-process", updated.getProcessInstanceId());
        verifyNoInteractions(noRedisDAO);
    }

    @Test
    public void testSubmitTravelApply_rollback() {
        // mock 数据
        OaTravelApplyDO record = randomTravelApplyDO().setProcessInstanceId(null);
        travelApplyMapper.insert(record);
        // 准备参数
        OaTravelApplySubmitReqVO reqVO = new OaTravelApplySubmitReqVO().setId(record.getId());
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenThrow(new IllegalStateException("流程未部署"));

        // 调用，并断言：失败不遗留审批中单据，也不覆盖草稿
        assertThrows(IllegalStateException.class, () -> travelApplyService.submitTravelApply(reqVO, 10L));
        OaTravelApplyDO updated = travelApplyMapper.selectById(record.getId());
        assertEquals(-1, updated.getStatus());
        assertEquals(record.getReason(), updated.getReason());
    }

    @Test
    public void testSubmitTravelApply_synchronousCallback() {
        // mock 数据
        OaTravelApplyDO record = randomTravelApplyDO();
        travelApplyMapper.insert(record);
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenAnswer(invocation -> {
            BpmProcessInstanceCreateReqDTO dto = invocation.getArgument(1);
            travelApplyService.updateTravelApplyStatus(Long.valueOf(dto.getBusinessKey()), 2);
            return "sync-process";
        });

        // 调用
        travelApplyService.submitTravelApply(new OaTravelApplySubmitReqVO().setId(record.getId()), 10L);

        // 断言：绑定流程编号不会把同步审批通过回退成审批中
        OaTravelApplyDO updated = travelApplyMapper.selectById(record.getId());
        assertEquals(2, updated.getStatus());
        assertEquals(false, updated.getReimburseStatus());
    }

    @Test
    public void testCancelTravelApply_running() {
        // mock 数据
        OaTravelApplyDO record = randomTravelApplyDO().setStatus(1).setProcessInstanceId("running");
        travelApplyMapper.insert(record);

        // 调用
        travelApplyService.cancelTravelApply(record.getId(), 10L);

        // 断言：通过 BPM 撤回，由事件回写状态
        verify(processInstanceApi).cancelProcessInstanceByStartUser(eq(10L), eq("running"), anyString());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 3, 4})
    public void testDeleteTravelApply_editable(Integer status) {
        // mock 数据
        OaTravelApplyDO record = randomTravelApplyDO().setStatus(status);
        travelApplyMapper.insert(record);

        // 调用
        travelApplyService.deleteTravelApply(record.getId(), 10L);
        // 断言
        assertNull(travelApplyMapper.selectById(record.getId()));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void testDeleteTravelApply_notEditable(Integer status) {
        // mock 数据
        OaTravelApplyDO record = randomTravelApplyDO().setStatus(status);
        travelApplyMapper.insert(record);

        // 调用，并断言异常
        assertServiceException(() -> travelApplyService.deleteTravelApply(record.getId(), 10L), TRAVEL_STATUS_INVALID);
    }

    @Test
    public void testGetTravelApply() {
        // mock 数据
        OaTravelApplyDO record = randomTravelApplyDO();
        travelApplyMapper.insert(record);

        // 调用，并断言：查询对齐用印，不在 Service 增加本人或流程参与人判断
        assertEquals(record.getId(), travelApplyService.getTravelApply(record.getId()).getId());
        assertNull(travelApplyService.getTravelApply(-1L));
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testUpdateAndDeleteTravelApply_notOwner() {
        // mock 数据
        OaTravelApplyDO record = randomTravelApplyDO();
        travelApplyMapper.insert(record);

        // 调用，并断言异常
        assertServiceException(() -> travelApplyService.updateTravelApply(toBean(record, OaTravelApplySaveReqVO.class), 20L),
                TRAVEL_ACCESS_DENIED);
        assertServiceException(() -> travelApplyService.deleteTravelApply(record.getId(), 20L), TRAVEL_ACCESS_DENIED);
    }

    @Test
    public void testGetTravelApplyPage_onlyOwner() {
        // mock 数据
        OaTravelApplyDO mine = randomTravelApplyDO().setNo("MINE");
        travelApplyMapper.insert(mine);
        OaTravelApplyDO other = randomTravelApplyDO().setNo("OTHER");
        other.setCreator("20");
        travelApplyMapper.insert(other);

        // 调用
        PageResult<OaTravelApplyDO> page = travelApplyService.getTravelApplyPage(10L, new OaTravelApplyPageReqVO());
        // 断言
        assertEquals(1L, page.getTotal());
        assertEquals(mine.getId(), CollUtil.getFirst(page.getList()).getId());
    }

    @Test
    public void testUpdateTravelApplyStatus_notExists() {

        // 调用，并断言异常
        assertServiceException(() -> travelApplyService.updateTravelApplyStatus(999L, 2), TRAVEL_NOT_EXISTS);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4})
    public void testUpdateTravelApplyStatus(Integer status) {
        // mock 数据
        OaTravelApplyDO record = randomTravelApplyDO().setStatus(1).setProcessInstanceId("current");
        travelApplyMapper.insert(record);

        // 调用
        travelApplyService.updateTravelApplyStatus(record.getId(), status);

        // 断言：按业务编号更新结果，不重新绑定流程编号
        OaTravelApplyDO updated = travelApplyMapper.selectById(record.getId());
        assertEquals(status, updated.getStatus());
        assertEquals("current", updated.getProcessInstanceId());
    }

    @Test
    public void testGetTravelApplyMap() {
        // mock 数据
        OaTravelApplyDO record = randomTravelApplyDO();
        travelApplyMapper.insert(record);

        // 调用
        Map<Long, OaTravelApplyDO> applies = travelApplyService.getTravelApplyMap(
                Arrays.asList(record.getId(), 999L));

        // 断言：按编号映射，忽略不存在的申请
        assertEquals(1, applies.size());
        assertEquals(record.getNo(), applies.get(record.getId()).getNo());
        assertEquals(Collections.emptyMap(), travelApplyService.getTravelApplyMap(Collections.emptyList()));
    }

    @Test
    public void testGetApprovedTravelDaysMap() {
        // mock 数据：月初申请和月末开始的跨月申请
        LocalDateTime startTime = LocalDateTime.of(2026, 9, 1, 0, 0);
        travelApplyMapper.insert(randomTravelApplyDO().setStatus(2).setStartTime(startTime).setDays(2));
        travelApplyMapper.insert(randomTravelApplyDO().setStatus(2).setStartTime(startTime.plusMonths(1).minusSeconds(1))
                .setEndTime(startTime.plusMonths(1).plusDays(2)).setDays(3));
        // 排除非通过、前后月份、其他用户和已删除的申请
        for (Integer status : Arrays.asList(-1, 1, 3, 4)) {
            travelApplyMapper.insert(randomTravelApplyDO().setStatus(status).setStartTime(startTime).setDays(10));
        }
        travelApplyMapper.insert(randomTravelApplyDO().setStatus(2).setStartTime(startTime.minusSeconds(1)).setDays(10));
        travelApplyMapper.insert(randomTravelApplyDO().setStatus(2).setStartTime(startTime.plusMonths(1)).setDays(10));
        OaTravelApplyDO otherUserApply = randomTravelApplyDO().setStatus(2).setStartTime(startTime).setDays(10);
        otherUserApply.setCreator("99");
        travelApplyMapper.insert(otherUserApply);
        OaTravelApplyDO deletedApply = randomTravelApplyDO().setStatus(2).setStartTime(startTime).setDays(10);
        deletedApply.setDeleted(true);
        travelApplyMapper.insert(deletedApply);
        // 准备参数
        LocalDateTime[] monthTime = getMonthDateTimeRange(2026, 9);

        // 调用
        Map<Long, Integer> result = travelApplyService.getApprovedTravelDaysMap(Arrays.asList(10L, 20L), monthTime);

        // 断言：按开始月份计入全部天数，不拆分跨月天数
        assertEquals(Collections.singletonMap(10L, 5), result);
        assertTrue(travelApplyService.getApprovedTravelDaysMap(Collections.emptyList(), monthTime).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"0,0", "120,1", "1440,1", "1441,2", "2880,2"})
    public void testCreateAndUpdateTravelApply_elapsedDays(Long minutes, Integer expectedDays) {
        // 准备参数：跨午夜不足一天、整天及超出整天的时间边界
        LocalDateTime startTime = LocalDateTime.of(2026, 9, 30, 23, 0);
        OaTravelApplySaveReqVO reqVO = toBean(randomTravelApplyDO(), OaTravelApplySaveReqVO.class)
                .setStartTime(startTime).setEndTime(startTime.plusMinutes(minutes));
        // mock 方法
        when(noRedisDAO.generate(anyString())).thenReturn("TRAVEL-DAYS");
        when(adminUserApi.validateUser(10L)).thenReturn(new AdminUserRespDTO().setId(10L).setDeptId(20L));

        // 调用
        Long id = travelApplyService.createTravelApply(reqVO, 10L);

        // 断言：按经过时长向上取整，不额外增加首尾日期
        assertEquals(expectedDays, travelApplyMapper.selectById(id).getDays());

        // 调用：修改开始、结束时间后重新计算
        reqVO.setId(id).setStartTime(startTime.plusMonths(1)).setEndTime(startTime.plusMonths(1).plusMinutes(minutes));
        travelApplyService.updateTravelApply(reqVO, 10L);

        // 断言
        assertEquals(expectedDays, travelApplyMapper.selectById(id).getDays());
    }

    @Test
    public void testCreateTravelApply_sameDayEndBeforeStart() {
        // 准备参数：同一天的结束时间早于开始时间
        LocalDateTime startTime = LocalDateTime.of(2026, 9, 30, 10, 0);
        OaTravelApplySaveReqVO reqVO = toBean(randomTravelApplyDO(), OaTravelApplySaveReqVO.class)
                .setStartTime(startTime).setEndTime(startTime.minusHours(1));

        // 调用，并断言：不能保存负时长的出差
        assertThrows(ConstraintViolationException.class,
                () -> travelApplyService.createTravelApply(reqVO, 10L));
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    // ========== 随机对象 ==========

    /**
     * 构造测试数据，固定业务字段的合法取值。
     *
     * @return 未入库的测试对象
     */
    private static OaTravelApplyDO randomTravelApplyDO() {
        OaTravelApplyDO record = randomPojo(OaTravelApplyDO.class, item -> item.setId(null)
                .setNo("TRAVEL-" + UUID.randomUUID()).setDeptId(20L)
                .setStartTime(LocalDateTime.of(2026, 9, 14, 0, 0)).setEndTime(LocalDateTime.of(2026, 9, 15, 0, 0))
                .setStatus(-1).setProcessInstanceId(null).setReimburseStatus(false)
                .setEstimatedPrice(BigDecimal.ZERO)
                .setItems(Collections.singletonList(new OaTravelApplyDO.Item()))
                .setFileUrls(Collections.singletonList("https://example.com/receipt.pdf")));
        record.setCreator("10");
        return record;
    }

}
