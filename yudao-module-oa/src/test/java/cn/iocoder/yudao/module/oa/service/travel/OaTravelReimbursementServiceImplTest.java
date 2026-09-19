package cn.iocoder.yudao.module.oa.service.travel;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.oa.controller.admin.travel.vo.reimbursement.*;
import cn.iocoder.yudao.module.oa.controller.admin.travel.vo.reimbursement.OaTravelReimbursementSubmitReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.travel.OaTravelReimbursementDO;
import cn.iocoder.yudao.module.oa.dal.mysql.travel.OaTravelReimbursementMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
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
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.util.object.BeanUtils.toBean;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaTravelReimbursementServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({OaTravelReimbursementServiceImpl.class, ValidationAutoConfiguration.class})
public class OaTravelReimbursementServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaTravelReimbursementService travelReimbursementService;

    @Resource
    private OaTravelReimbursementMapper travelReimbursementMapper;

    @MockitoBean
    private OaNoRedisDAO noRedisDAO;
    @MockitoBean
    private AdminUserApi adminUserApi;
    @MockitoBean
    private DictDataApi dictDataApi;
    @MockitoBean
    private BpmProcessInstanceApi processInstanceApi;
    @MockitoBean
    private OaTravelApplyService travelApplyService;

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
    public void testCreateTravelReimbursement_duplicateNo() {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO();
        travelReimbursementMapper.insert(record);
        // mock 方法
        when(noRedisDAO.generate(anyString())).thenReturn(record.getNo());
        when(adminUserApi.validateUser(10L)).thenReturn(new AdminUserRespDTO().setId(10L).setDeptId(20L));

        // 调用，并断言异常
        assertServiceException(() -> travelReimbursementService.createTravelReimbursement(toBean(randomTravelReimbursementDO(), OaTravelReimbursementSaveReqVO.class), 10L),
                TRAVEL_NO_DUPLICATE);
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testSubmitTravelReimbursement_savedContentAndAssignees() {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO().setDays(2);
        travelReimbursementMapper.insert(record);
        // 准备参数
        OaTravelReimbursementSubmitReqVO reqVO = new OaTravelReimbursementSubmitReqVO().setId(record.getId())
                .setStartUserSelectAssignees(Collections.singletonMap("approve", Collections.singletonList(20L)));
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("new-process");

        // 调用
        travelReimbursementService.submitTravelReimbursement(reqVO, 10L);

        // 断言：仅提交审批，不覆盖正文及明细
        OaTravelReimbursementDO updated = travelReimbursementMapper.selectById(record.getId());
        assertEquals(record.getReason(), updated.getReason());
        assertEquals(record.getItems(), updated.getItems());
        assertEquals(record.getFileUrls(), updated.getFileUrls());
        verify(processInstanceApi).createProcessInstance(eq(10L), argThat(dto ->
                Integer.valueOf(2).equals(dto.getVariables().get("days"))
                        && reqVO.getStartUserSelectAssignees().equals(dto.getStartUserSelectAssignees())));
    }

    @Test
    public void testSubmitTravelReimbursement_notOwner() {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO();
        travelReimbursementMapper.insert(record);
        // 准备参数
        OaTravelReimbursementSubmitReqVO reqVO = new OaTravelReimbursementSubmitReqVO().setId(record.getId());

        // 调用，并断言异常
        assertServiceException(() -> travelReimbursementService.submitTravelReimbursement(reqVO, 20L), TRAVEL_ACCESS_DENIED);
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testCreateTravelReimbursement_jsonAndDays() {
        // 准备参数
        OaTravelReimbursementSaveReqVO reqVO = toBean(randomTravelReimbursementDO(), OaTravelReimbursementSaveReqVO.class);
        // mock 方法
        when(noRedisDAO.generate(anyString())).thenReturn("TRAVEL-NEW");
        when(adminUserApi.validateUser(10L)).thenReturn(new AdminUserRespDTO().setId(10L).setDeptId(20L));

        // 调用
        Long id = travelReimbursementService.createTravelReimbursement(reqVO, 10L);

        // 断言：新增保存明细、附件并计算天数和总金额
        OaTravelReimbursementDO record = travelReimbursementMapper.selectById(id);
        assertEquals(1, record.getDays());
        assertEquals(reqVO.getItems().size(), record.getItems().size());
        assertEquals(reqVO.getFileUrls(), record.getFileUrls());
        assertEquals(new BigDecimal("0.00"), record.getTotalPrice());
        assertEquals("TRAVEL-NEW", record.getNo());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testUpdateTravelReimbursement_notExists() {
        // 准备参数
        OaTravelReimbursementSaveReqVO reqVO = toBean(randomTravelReimbursementDO(), OaTravelReimbursementSaveReqVO.class)
                .setId(999L);

        // 调用，并断言：修改不存在的单据不会转为新增
        assertServiceException(() -> travelReimbursementService.updateTravelReimbursement(reqVO, 10L), TRAVEL_NOT_EXISTS);
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    @Test
    public void testSubmitTravelReimbursement_unsaved() {
        // 准备参数
        OaTravelReimbursementSubmitReqVO reqVO = new OaTravelReimbursementSubmitReqVO().setId(999L);

        // 调用，并断言异常
        assertServiceException(() -> travelReimbursementService.submitTravelReimbursement(reqVO, 10L), TRAVEL_NOT_EXISTS);
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    @Test
    public void testCreateTravelReimbursement_nullItem() {
        // 准备参数
        OaTravelReimbursementSaveReqVO reqVO = toBean(randomTravelReimbursementDO(), OaTravelReimbursementSaveReqVO.class)
                .setItems(Collections.singletonList(null));

        // 调用，并断言：空明细由级联校验拦截，不进入字典查询及保存
        assertThrows(ConstraintViolationException.class, () -> travelReimbursementService.createTravelReimbursement(reqVO, 10L));
        verifyNoInteractions(dictDataApi, noRedisDAO, processInstanceApi);
    }

    @Test
    public void testCreateTravelReimbursement_emptyDraft() {
        // 准备参数
        OaTravelReimbursementSaveReqVO reqVO = new OaTravelReimbursementSaveReqVO();

        // 调用，并断言：草稿保存也必须填写完整内容
        assertThrows(ConstraintViolationException.class, () -> travelReimbursementService.createTravelReimbursement(reqVO, 10L));
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    @Test
    public void testUpdateTravelReimbursement_jsonAndDays() {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO();
        travelReimbursementMapper.insert(record);
        // 准备参数
        OaTravelReimbursementSaveReqVO reqVO = toBean(record, OaTravelReimbursementSaveReqVO.class)
                .setStartTime(LocalDateTime.of(2026, 12, 31, 0, 0))
                .setEndTime(LocalDateTime.of(2027, 1, 2, 0, 0))
                .setFileUrls(Collections.singletonList("https://example.com/receipt.pdf"));

        // 调用
        travelReimbursementService.updateTravelReimbursement(reqVO, 10L);

        // 断言：跨年按实际时长计算天数，JSON 明细正常读取
        OaTravelReimbursementDO updated = travelReimbursementMapper.selectById(record.getId());
        assertEquals(2, updated.getDays());
        assertEquals(1, updated.getItems().size());
        assertEquals(reqVO.getFileUrls(), updated.getFileUrls());
        assertEquals(record.getNo(), updated.getNo());
    }

    @Test
    public void testUpdateTravelReimbursement_missingRequiredFields() {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO();
        travelReimbursementMapper.insert(record);
        // 准备参数
        OaTravelReimbursementSaveReqVO reqVO = toBean(record, OaTravelReimbursementSaveReqVO.class).setReason(null);

        // 调用，并断言：修改不能清空必填内容，已有数据保持不变
        assertThrows(ConstraintViolationException.class, () -> travelReimbursementService.updateTravelReimbursement(reqVO, 10L));
        OaTravelReimbursementDO updated = travelReimbursementMapper.selectById(record.getId());
        assertEquals(record.getReason(), updated.getReason());
        assertEquals(record.getStatus(), updated.getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testCreateTravelReimbursement_invalidDates() {
        // 准备参数
        OaTravelReimbursementSaveReqVO reqVO = toBean(randomTravelReimbursementDO(), OaTravelReimbursementSaveReqVO.class)
                .setId(null)
                .setStartTime(LocalDateTime.of(2026, 9, 15, 0, 0)).setEndTime(LocalDateTime.of(2026, 9, 14, 0, 0));

        // 调用，并断言
        assertThrows(ConstraintViolationException.class, () -> travelReimbursementService.createTravelReimbursement(reqVO, 10L));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void testUpdateTravelReimbursement_notEditable(Integer status) {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO().setStatus(status);
        travelReimbursementMapper.insert(record);
        // 准备参数
        OaTravelReimbursementSaveReqVO reqVO = toBean(record, OaTravelReimbursementSaveReqVO.class);

        // 调用，并断言异常
        assertServiceException(() -> travelReimbursementService.updateTravelReimbursement(reqVO, 10L), TRAVEL_STATUS_INVALID);
    }

    @Test
    public void testCreateTravelReimbursement_missingFields() {
        // 准备参数
        OaTravelReimbursementSaveReqVO reqVO = toBean(randomTravelReimbursementDO().setReason(null)
                .setStartTime(null), OaTravelReimbursementSaveReqVO.class);

        // 调用，并断言：必填校验在保存时完成，不生成单号或发起审批
        assertThrows(ConstraintViolationException.class, () -> travelReimbursementService.createTravelReimbursement(reqVO, 10L));
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    @Test
    public void testCreateTravelReimbursement_missingItems() {
        // 准备参数
        OaTravelReimbursementSaveReqVO reqVO = toBean(randomTravelReimbursementDO()
                .setItems(Collections.emptyList()), OaTravelReimbursementSaveReqVO.class);

        // 调用，并断言：必填校验在保存时完成，不生成单号或发起审批
        assertThrows(ConstraintViolationException.class, () -> travelReimbursementService.createTravelReimbursement(reqVO, 10L));
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 3, 4})
    public void testSubmitTravelReimbursement_resubmit(Integer status) {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO().setStatus(status).setProcessInstanceId("old-process");
        travelReimbursementMapper.insert(record);
        // 准备参数
        OaTravelReimbursementSubmitReqVO reqVO = new OaTravelReimbursementSubmitReqVO().setId(record.getId());
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("new-process");

        // 调用
        travelReimbursementService.submitTravelReimbursement(reqVO, 10L);
        Long id = record.getId();

        // 断言：不重复创建单据，使用新的审批实例
        OaTravelReimbursementDO updated = travelReimbursementMapper.selectById(id);
        assertEquals(record.getId(), id);
        assertEquals(1, updated.getStatus());
        assertEquals("new-process", updated.getProcessInstanceId());
        verifyNoInteractions(noRedisDAO);
    }

    @Test
    public void testSubmitTravelReimbursement_rollback() {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO().setProcessInstanceId(null);
        travelReimbursementMapper.insert(record);
        // 准备参数
        OaTravelReimbursementSubmitReqVO reqVO = new OaTravelReimbursementSubmitReqVO().setId(record.getId());
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenThrow(new IllegalStateException("流程未部署"));

        // 调用，并断言：失败不遗留审批中单据，也不覆盖草稿
        assertThrows(IllegalStateException.class, () -> travelReimbursementService.submitTravelReimbursement(reqVO, 10L));
        OaTravelReimbursementDO updated = travelReimbursementMapper.selectById(record.getId());
        assertEquals(-1, updated.getStatus());
        assertEquals(record.getReason(), updated.getReason());
    }

    @Test
    public void testSubmitTravelReimbursement_synchronousCallback() {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO();
        travelReimbursementMapper.insert(record);
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenAnswer(invocation -> {
            BpmProcessInstanceCreateReqDTO dto = invocation.getArgument(1);
            travelReimbursementService.updateTravelReimbursementStatus(Long.valueOf(dto.getBusinessKey()), 2);
            return "sync-process";
        });

        // 调用
        travelReimbursementService.submitTravelReimbursement(new OaTravelReimbursementSubmitReqVO().setId(record.getId()), 10L);

        // 断言：绑定流程编号不会把同步审批通过回退成审批中
        OaTravelReimbursementDO updated = travelReimbursementMapper.selectById(record.getId());
        assertEquals(2, updated.getStatus());
        assertEquals(false, updated.getPayStatus());
    }

    @Test
    public void testCancelTravelReimbursement_running() {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO().setStatus(1).setProcessInstanceId("running");
        travelReimbursementMapper.insert(record);

        // 调用
        travelReimbursementService.cancelTravelReimbursement(record.getId(), 10L);

        // 断言：通过 BPM 撤回，由事件回写状态
        verify(processInstanceApi).cancelProcessInstanceByStartUser(eq(10L), eq("running"), anyString());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 3, 4})
    public void testDeleteTravelReimbursement_editable(Integer status) {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO().setStatus(status);
        travelReimbursementMapper.insert(record);

        // 调用
        travelReimbursementService.deleteTravelReimbursement(record.getId(), 10L);
        // 断言
        assertNull(travelReimbursementMapper.selectById(record.getId()));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void testDeleteTravelReimbursement_notEditable(Integer status) {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO().setStatus(status);
        travelReimbursementMapper.insert(record);

        // 调用，并断言异常
        assertServiceException(() -> travelReimbursementService.deleteTravelReimbursement(record.getId(), 10L),
                TRAVEL_STATUS_INVALID);
    }

    @Test
    public void testGetTravelReimbursement() {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO();
        travelReimbursementMapper.insert(record);

        // 调用，并断言：查询对齐用印，不在 Service 增加本人或流程参与人判断
        assertEquals(record.getId(), travelReimbursementService.getTravelReimbursement(record.getId()).getId());
        assertNull(travelReimbursementService.getTravelReimbursement(-1L));
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testUpdateAndDeleteTravelReimbursement_notOwner() {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO();
        travelReimbursementMapper.insert(record);

        // 调用，并断言异常
        assertServiceException(() -> travelReimbursementService.updateTravelReimbursement(toBean(record, OaTravelReimbursementSaveReqVO.class), 20L),
                TRAVEL_ACCESS_DENIED);
        assertServiceException(() -> travelReimbursementService.deleteTravelReimbursement(record.getId(), 20L),
                TRAVEL_ACCESS_DENIED);
    }

    @Test
    public void testGetTravelReimbursementPage_onlyOwner() {
        // mock 数据
        OaTravelReimbursementDO mine = randomTravelReimbursementDO().setNo("MINE");
        travelReimbursementMapper.insert(mine);
        OaTravelReimbursementDO other = randomTravelReimbursementDO().setNo("OTHER");
        other.setCreator("20");
        travelReimbursementMapper.insert(other);

        // 调用
        PageResult<OaTravelReimbursementDO> page = travelReimbursementService.getTravelReimbursementPage(10L, new OaTravelReimbursementPageReqVO());
        // 断言
        assertEquals(1L, page.getTotal());
        assertEquals(mine.getId(), CollUtil.getFirst(page.getList()).getId());
    }

    @Test
    public void testUpdateTravelReimbursementStatus_notExists() {

        // 调用，并断言异常
        assertServiceException(() -> travelReimbursementService.updateTravelReimbursementStatus(999L, 2), TRAVEL_NOT_EXISTS);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4})
    public void testUpdateTravelReimbursementStatus(Integer status) {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO().setStatus(1).setProcessInstanceId("current");
        travelReimbursementMapper.insert(record);

        // 调用
        travelReimbursementService.updateTravelReimbursementStatus(record.getId(), status);

        // 断言：按业务编号更新结果，不重新绑定流程编号
        OaTravelReimbursementDO updated = travelReimbursementMapper.selectById(record.getId());
        assertEquals(status, updated.getStatus());
        assertEquals("current", updated.getProcessInstanceId());
    }

    @Test
    public void testCreateTravelReimbursement_invalidExpenseType() {
        // 准备参数
        OaTravelReimbursementSaveReqVO reqVO = toBean(randomTravelReimbursementDO(), OaTravelReimbursementSaveReqVO.class)
                .setItems(Collections.singletonList(
                new OaTravelReimbursementSaveReqVO.Item().setExpenseType(999)));
        // mock 方法
        when(dictDataApi.getDictDataList(anyString())).thenReturn(Collections.emptyList());

        // 调用，并断言：保存入口通过 InDict 校验费用类型
        assertThrows(ConstraintViolationException.class, () -> travelReimbursementService.createTravelReimbursement(reqVO, 10L));
        verify(dictDataApi, never()).validateDictDataList(anyString(), anyCollection());
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    @Test
    public void testCreateTravelReimbursement_missingPrice() {
        // 准备参数
        OaTravelReimbursementSaveReqVO reqVO = toBean(randomTravelReimbursementDO()
                .setItems(Collections.singletonList(new OaTravelReimbursementDO.Item())), OaTravelReimbursementSaveReqVO.class);

        // 调用，并断言：必填校验在保存时完成，不生成单号或发起审批
        assertThrows(ConstraintViolationException.class, () -> travelReimbursementService.createTravelReimbursement(reqVO, 10L));
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    @Test
    public void testCreateTravelReimbursement_missingFiles() {
        // 准备参数
        OaTravelReimbursementSaveReqVO reqVO = toBean(randomTravelReimbursementDO()
                .setFileUrls(Collections.emptyList()), OaTravelReimbursementSaveReqVO.class);

        // 调用，并断言：必填校验在保存时完成，不生成单号或发起审批
        assertThrows(ConstraintViolationException.class, () -> travelReimbursementService.createTravelReimbursement(reqVO, 10L));
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    @Test
    public void testUpdateTravelReimbursement_totalPrice() {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO();
        travelReimbursementMapper.insert(record);
        // 准备参数
        OaTravelReimbursementSaveReqVO reqVO = toBean(record, OaTravelReimbursementSaveReqVO.class);
        reqVO.setItems(Arrays.asList(
                new OaTravelReimbursementSaveReqVO.Item().setPrice(new BigDecimal("0.10")),
                new OaTravelReimbursementSaveReqVO.Item().setPrice(new BigDecimal("0.20"))));

        // 调用
        travelReimbursementService.updateTravelReimbursement(reqVO, 10L);
        // 断言：汇总使用十进制，避免金额浮点误差
        assertEquals(new BigDecimal("0.30"), travelReimbursementMapper.selectById(record.getId()).getTotalPrice());
    }

    @Test
    public void testCreateTravelReimbursement_negativePrice() {
        // 准备参数
        OaTravelReimbursementSaveReqVO reqVO = toBean(randomTravelReimbursementDO(), OaTravelReimbursementSaveReqVO.class)
                .setItems(Collections.singletonList(new OaTravelReimbursementSaveReqVO.Item().setPrice(BigDecimal.ONE.negate())));

        // 调用，并断言
        assertThrows(ConstraintViolationException.class, () -> travelReimbursementService.createTravelReimbursement(reqVO, 10L));
    }

    @Test
    public void testUpdateTravelReimbursement_selectedApply() {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO();
        travelReimbursementMapper.insert(record);
        // 准备参数
        OaTravelReimbursementSaveReqVO reqVO = toBean(record, OaTravelReimbursementSaveReqVO.class);
        reqVO.setTravelApplyId(100L);

        // 调用
        travelReimbursementService.updateTravelReimbursement(reqVO, 10L);
        // 断言：关联申请由申请 Service 校验，不直接操作其他聚合的 Mapper
        verify(travelApplyService).validateApprovedTravelApply(100L, 10L);
        assertEquals(100L, travelReimbursementMapper.selectById(record.getId()).getTravelApplyId());
    }

    @Test
    public void testUpdateTravelReimbursementStatus_approved() {
        // mock 数据
        OaTravelReimbursementDO record = randomTravelReimbursementDO()
                .setStatus(1).setProcessInstanceId("current").setTravelApplyId(100L);
        travelReimbursementMapper.insert(record);

        // 调用
        travelReimbursementService.updateTravelReimbursementStatus(record.getId(), 2);
        travelReimbursementService.updateTravelReimbursementStatus(record.getId(), 2);
        // 断言：审批通过只标记已报销，不视作已支付；重复回调不重复更新关联申请
        OaTravelReimbursementDO actual = travelReimbursementMapper.selectById(record.getId());
        assertEquals(2, actual.getStatus());
        assertEquals(false, actual.getPayStatus());
        verify(travelApplyService, times(1)).updateTravelApplyReimburseStatus(100L);
    }

    @ParameterizedTest
    @CsvSource({"0,0", "120,1", "1440,1", "1441,2", "2880,2"})
    public void testCreateAndUpdateTravelReimbursement_elapsedDays(Long minutes, Integer expectedDays) {
        // 准备参数：跨午夜不足一天、整天及超出整天的时间边界
        LocalDateTime startTime = LocalDateTime.of(2026, 9, 30, 23, 0);
        OaTravelReimbursementSaveReqVO reqVO = toBean(randomTravelReimbursementDO(), OaTravelReimbursementSaveReqVO.class)
                .setStartTime(startTime).setEndTime(startTime.plusMinutes(minutes));
        // mock 方法
        when(noRedisDAO.generate(anyString())).thenReturn("TRAVEL-DAYS");
        when(adminUserApi.validateUser(10L)).thenReturn(new AdminUserRespDTO().setId(10L).setDeptId(20L));

        // 调用
        Long id = travelReimbursementService.createTravelReimbursement(reqVO, 10L);

        // 断言：按经过时长向上取整，不额外增加首尾日期
        assertEquals(expectedDays, travelReimbursementMapper.selectById(id).getDays());

        // 调用：修改开始、结束时间后重新计算
        reqVO.setId(id).setStartTime(startTime.plusMonths(1)).setEndTime(startTime.plusMonths(1).plusMinutes(minutes));
        travelReimbursementService.updateTravelReimbursement(reqVO, 10L);

        // 断言
        assertEquals(expectedDays, travelReimbursementMapper.selectById(id).getDays());
    }

    @Test
    public void testCreateTravelReimbursement_sameDayEndBeforeStart() {
        // 准备参数：同一天的结束时间早于开始时间
        LocalDateTime startTime = LocalDateTime.of(2026, 9, 30, 10, 0);
        OaTravelReimbursementSaveReqVO reqVO = toBean(randomTravelReimbursementDO(), OaTravelReimbursementSaveReqVO.class)
                .setStartTime(startTime).setEndTime(startTime.minusHours(1));

        // 调用，并断言：不能保存负时长的出差
        assertThrows(ConstraintViolationException.class,
                () -> travelReimbursementService.createTravelReimbursement(reqVO, 10L));
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    // ========== 随机对象 ==========

    /**
     * 构造测试数据，固定业务字段的合法取值。
     *
     * @return 未入库的测试对象
     */
    private static OaTravelReimbursementDO randomTravelReimbursementDO() {
        OaTravelReimbursementDO record = randomPojo(OaTravelReimbursementDO.class, item -> item.setId(null)
                .setNo("TRAVEL-" + UUID.randomUUID()).setDeptId(20L)
                .setStartTime(LocalDateTime.of(2026, 9, 14, 0, 0)).setEndTime(LocalDateTime.of(2026, 9, 15, 0, 0))
                .setStatus(-1).setProcessInstanceId(null).setPayStatus(false)
                .setTravelApplyId(null).setTotalPrice(BigDecimal.ZERO)
                .setItems(Collections.singletonList(new OaTravelReimbursementDO.Item().setPrice(BigDecimal.ZERO)))
                .setFileUrls(Collections.singletonList("https://example.com/receipt.pdf")));
        record.setCreator("10");
        return record;
    }

}
