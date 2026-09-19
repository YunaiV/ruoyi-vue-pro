package cn.iocoder.yudao.module.oa.service.seal;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.oa.controller.admin.seal.vo.apply.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.seal.*;
import cn.iocoder.yudao.module.oa.dal.mysql.seal.OaSealApplyMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.service.seal.listener.OaSealApplyStatusListener;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.common.util.object.BeanUtils.toBean;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaSealApplyServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({OaSealApplyServiceImpl.class, OaSealApplyStatusListener.class})
public class OaSealApplyServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private OaSealApplyService sealApplyService;

    @Resource
    private OaSealApplyMapper sealApplyMapper;

    @MockitoBean
    private OaSealService sealService;
    @MockitoBean
    private AdminUserApi adminUserApi;
    @MockitoBean
    private BpmProcessInstanceApi processInstanceApi;
    @MockitoBean
    private OaNoRedisDAO noRedisDAO;

    @Test
    public void testCreateSealApply() {
        // 准备参数
        OaSealApplySaveReqVO reqVO = toBean(randomSealApplyDO(), OaSealApplySaveReqVO.class)
                .setId(999L).setActualReturnTime(LocalDateTime.now().minusHours(1).withNano(0))
                .setFileUrls(Arrays.asList("https://example.com/a.pdf", "https://example.com/b.pdf"));
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());
        when(adminUserApi.getUser(10L)).thenReturn(new AdminUserRespDTO().setId(10L).setDeptId(20L));
        when(noRedisDAO.generate(OaNoRedisDAO.SEAL_APPLY_NO_PREFIX)).thenReturn("YY20260912000001");

        // 调用
        Long id = sealApplyService.createSealApply(reqVO, 10L);
        // 断言
        OaSealApplyDO apply = sealApplyMapper.selectById(id);
        assertNotEquals(999L, id);
        assertEquals("YY20260912000001", apply.getNo());
        verify(noRedisDAO).generate(OaNoRedisDAO.SEAL_APPLY_NO_PREFIX);
        assertEquals(10L, apply.getUserId());
        assertEquals(20L, apply.getDeptId());
        assertEquals(40L, apply.getKeeperUserId());
        assertEquals(1, apply.getSealType());
        assertEquals(20L, apply.getKeeperDeptId());
        assertEquals(-1, apply.getStatus());
        assertEquals(0, apply.getUseStatus());
        assertNull(apply.getProcessInstanceId());
        assertNull(apply.getActualUseTime());
        assertEquals(reqVO.getActualReturnTime(), apply.getActualReturnTime());
        assertEquals(reqVO.getFileUrls(), apply.getFileUrls());
    }

    @Test
    public void testUpdateSealApply_clearFiles() {
        // mock 数据
        OaSealApplyDO apply = randomSealApplyDO().setFileUrls(Arrays.asList("https://example.com/a.pdf"));
        sealApplyMapper.insert(apply);
        // 准备参数
        OaSealApplySaveReqVO reqVO = toBean(apply, OaSealApplySaveReqVO.class).setFileUrls(Collections.emptyList())
                .setActualReturnTime(LocalDateTime.now().minusMinutes(10).withNano(0));
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());

        // 调用
        sealApplyService.updateSealApply(reqVO, 10L);
        // 断言
        assertEquals(Collections.emptyList(), sealApplyMapper.selectById(apply.getId()).getFileUrls());
        assertEquals(reqVO.getActualReturnTime(), sealApplyMapper.selectById(apply.getId()).getActualReturnTime());
        assertEquals(0, sealApplyMapper.selectById(apply.getId()).getUseStatus());
    }

    @Test
    public void testCreateSealApply_contractOptional() {
        // 准备参数
        OaSealApplySaveReqVO reqVO = toBean(randomSealApplyDO(), OaSealApplySaveReqVO.class)
                .setContractPrice(null).setContractParty(null).setDocumentTitle(null);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());
        when(adminUserApi.getUser(10L)).thenReturn(new AdminUserRespDTO().setId(10L).setDeptId(20L));
        when(noRedisDAO.generate(OaNoRedisDAO.SEAL_APPLY_NO_PREFIX)).thenReturn("YY20260912000001");

        // 调用
        Long id = sealApplyService.createSealApply(reqVO, 10L);
        // 断言
        assertNull(sealApplyMapper.selectById(id).getContractPrice());
        assertNull(sealApplyMapper.selectById(id).getContractParty());
    }

    @Test
    public void testCreateSealApply_borrowReturnOptional() {
        // 准备参数
        OaSealApplySaveReqVO reqVO = toBean(randomSealApplyDO(), OaSealApplySaveReqVO.class).setExpectedReturnTime(null);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());
        when(adminUserApi.getUser(10L)).thenReturn(new AdminUserRespDTO().setId(10L).setDeptId(20L));
        when(noRedisDAO.generate(OaNoRedisDAO.SEAL_APPLY_NO_PREFIX)).thenReturn("YY20260912000001");

        // 调用
        Long id = sealApplyService.createSealApply(reqVO, 10L);
        // 断言
        assertNull(sealApplyMapper.selectById(id).getExpectedReturnTime());
    }

    @Test
    public void testCreateSealApply_disabled() {
        // 准备参数
        OaSealApplySaveReqVO reqVO = toBean(randomSealApplyDO(), OaSealApplySaveReqVO.class);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO().setStatus(1));
        when(adminUserApi.getUser(10L)).thenReturn(new AdminUserRespDTO().setId(10L).setDeptId(20L));
        when(noRedisDAO.generate(OaNoRedisDAO.SEAL_APPLY_NO_PREFIX)).thenReturn("YY20260912000001");

        // 调用，并断言：台账状态不额外限制申请
        assertNotNull(sealApplyService.createSealApply(reqVO, 10L));
    }

    @Test
    public void testSubmitSealApply_autoApprove() {
        // mock 数据
        OaSealApplyDO apply = randomSealApplyDO().setProcessInstanceId(null);
        sealApplyMapper.insert(apply);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenAnswer(invocation -> {
            // 模拟流程创建返回前同步发布审批通过事件
            eventPublisher.publishEvent(new BpmProcessInstanceStatusEvent(this).setId("seal-auto-process")
                    .setProcessDefinitionKey(BpmModelConstants.SEAL_APPLY)
                    .setBusinessKey(apply.getId().toString()).setStatus(2));
            return "seal-auto-process";
        });

        // 调用
        String processInstanceId = sealApplyService.submitSealApply(apply.getId(), 10L);
        // 断言
        OaSealApplyDO updated = sealApplyMapper.selectById(apply.getId());
        assertEquals("seal-auto-process", processInstanceId);
        assertEquals(processInstanceId, updated.getProcessInstanceId());
        assertEquals(2, updated.getStatus());
    }

    @Test
    public void testSubmitSealApply_repeated() {
        // mock 数据
        OaSealApplyDO apply = randomSealApplyDO();
        sealApplyMapper.insert(apply);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenAnswer(invocation -> {
            BpmProcessInstanceCreateReqDTO reqDTO = invocation.getArgument(1);
            // BPM 会追加流程内置变量，调用方必须提供可写的变量集合
            reqDTO.getVariables().put("PROCESS_START_USER_ID", 10L);
            return "seal-process";
        });

        // 调用
        assertEquals("seal-process", sealApplyService.submitSealApply(apply.getId(), 10L));
        assertServiceException(() -> sealApplyService.submitSealApply(apply.getId(), 10L), SEAL_APPLY_STATUS_INVALID);
        // 断言
        verify(processInstanceApi, times(1)).createProcessInstance(eq(10L), any());
        assertEquals(1, sealApplyMapper.selectById(apply.getId()).getStatus());
    }

    @Test
    public void testSubmitSealApply_approvedPendingConflict() {
        // mock 数据：已有申请审批通过，但不会自动流转用印状态
        OaSealApplyDO other = randomSealApplyDO().setStatus(1);
        OaSealApplyDO apply = randomSealApplyDO();
        sealApplyMapper.insert(other);
        sealApplyMapper.insert(apply);
        sealApplyService.updateSealApplyStatus(other.getId(), 2);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());

        // 调用，并断言：审批通过后仍占用预计外借时段
        assertServiceException(() -> sealApplyService.submitSealApply(apply.getId(), 10L), SEAL_APPLY_TIME_CONFLICT);
        assertEquals(0, sealApplyMapper.selectById(other.getId()).getUseStatus());
        assertEquals(-1, sealApplyMapper.selectById(apply.getId()).getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4})
    public void testSubmitSealApply_approvedConflictIndependentOfUseStatus(int useStatus) {
        // mock 数据：审批通过后的预计时段不依赖历史用印状态
        sealApplyMapper.insert(randomSealApplyDO().setStatus(2).setUseStatus(useStatus));
        OaSealApplyDO apply = randomSealApplyDO();
        sealApplyMapper.insert(apply);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());

        // 调用，并断言：即使历史记录为已归还，也按预计结束时间释放预约
        assertServiceException(() -> sealApplyService.submitSealApply(apply.getId(), 10L), SEAL_APPLY_TIME_CONFLICT);
        verifyNoInteractions(processInstanceApi);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 3, 4})
    public void testSubmitSealApply_inactiveApplicationDoesNotOccupy(int status) {
        // mock 数据：草稿、驳回及撤销的外借申请不占用预约
        sealApplyMapper.insert(randomSealApplyDO().setStatus(status).setUseStatus(2));
        OaSealApplyDO apply = randomSealApplyDO();
        sealApplyMapper.insert(apply);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("no-conflict");

        // 调用，并断言
        assertEquals("no-conflict", sealApplyService.submitSealApply(apply.getId(), 10L));
    }

    @Test
    public void testSubmitSealApply_conflict() {
        // mock 数据
        OaSealApplyDO other = randomSealApplyDO().setStatus(1);
        OaSealApplyDO apply = randomSealApplyDO();
        sealApplyMapper.insert(other);
        sealApplyMapper.insert(apply);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());

        // 调用及断言
        assertServiceException(() -> sealApplyService.submitSealApply(apply.getId(), 10L), SEAL_APPLY_TIME_CONFLICT);
        verifyNoInteractions(processInstanceApi);
        assertEquals(-1, sealApplyMapper.selectById(apply.getId()).getStatus());
    }

    @Test
    public void testSubmitSealApply_approvedPendingBoundaryConflict() {
        // mock 数据：预计归还时刻与新申请用印时刻相同，闭区间仍重叠
        OaSealApplyDO other = randomSealApplyDO().setStatus(2);
        OaSealApplyDO apply = randomSealApplyDO().setExpectedUseTime(other.getExpectedReturnTime())
                .setExpectedReturnTime(other.getExpectedReturnTime().plusHours(1));
        sealApplyMapper.insert(other);
        sealApplyMapper.insert(apply);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());

        // 调用，并断言异常
        assertServiceException(() -> sealApplyService.submitSealApply(apply.getId(), 10L), SEAL_APPLY_TIME_CONFLICT);
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testSubmitSealApply_bpmFailureRollsBack() {
        // mock 数据
        OaSealApplyDO apply = randomSealApplyDO();
        sealApplyMapper.insert(apply);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenThrow(new IllegalStateException("test"));

        // 调用及断言
        assertThrows(IllegalStateException.class, () -> sealApplyService.submitSealApply(apply.getId(), 10L));
        assertEquals(-1, sealApplyMapper.selectById(apply.getId()).getStatus());
        assertNull(sealApplyMapper.selectById(apply.getId()).getProcessInstanceId());
    }

    @Test
    public void testUpdateSealApplyStatus_approveNotUsed() {
        // mock 数据
        OaSealApplyDO apply = randomSealApplyDO().setStatus(1).setProcessInstanceId("p1");
        sealApplyMapper.insert(apply);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());

        // 调用
        sealApplyService.updateSealApplyStatus(apply.getId(), 2);
        sealApplyService.updateSealApplyStatus(apply.getId(), 2);
        // 断言
        OaSealApplyDO result = sealApplyMapper.selectById(apply.getId());
        assertEquals(2, result.getStatus());
        assertEquals(0, result.getUseStatus());
        assertNull(result.getActualUseTime());
    }

    @Test
    public void testUpdateSealApplyStatus_notExists() {

        // 调用，并断言异常
        assertServiceException(() -> sealApplyService.updateSealApplyStatus(-1L, 2), SEAL_APPLY_NOT_EXISTS);
    }

    @Test
    public void testGetSealApply() {
        // mock 数据
        OaSealApplyDO apply = randomSealApplyDO().setStatus(1).setProcessInstanceId("p1");
        sealApplyMapper.insert(apply);

        // 调用及断言
        assertEquals(apply.getId(), sealApplyService.getSealApply(apply.getId()).getId());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testGetSealApply_notExists() {

        // 调用及断言：与 OA leave 一致，不存在时返回空
        assertNull(sealApplyService.getSealApply(999L));
    }

    @Test
    public void testDeleteSealApply_ownerOnly() {
        // mock 数据
        OaSealApplyDO apply = randomSealApplyDO();
        sealApplyMapper.insert(apply);

        // 调用及断言
        assertServiceException(() -> sealApplyService.deleteSealApply(apply.getId(), 40L), SEAL_APPLY_NOT_OWNER);
        sealApplyService.deleteSealApply(apply.getId(), 10L);
        assertNull(sealApplyMapper.selectById(apply.getId()));
    }

    @Test
    public void testGetSealApplyPage_userScope() {
        // mock 数据
        OaSealApplyDO mine = randomSealApplyDO();
        OaSealApplyDO other = randomSealApplyDO().setUserId(99L).setKeeperUserId(10L);
        sealApplyMapper.insert(mine);
        sealApplyMapper.insert(other);
        // 准备参数
        OaSealApplyPageReqVO reqVO = new OaSealApplyPageReqVO();

        // 调用
        PageResult<OaSealApplyDO> pageResult = sealApplyService.getSealApplyPage(10L, reqVO);
        // 断言：仅返回本人申请，即使其他申请由本人保管也不包含在列表中
        assertEquals(1L, pageResult.getTotal());
        assertEquals(mine.getId(), CollUtil.getFirst(pageResult.getList()).getId());
    }

    @Test
    public void testUpdateSealApply_keepNullFields() {
        // mock 数据
        OaSealApplyDO apply = randomSealApplyDO().setType(1).setMode(2)
                .setExpectedReturnTime(LocalDateTime.now().plusDays(3).withNano(0));
        sealApplyMapper.insert(apply);
        // 准备参数
        OaSealApplySaveReqVO reqVO = toBean(apply, OaSealApplySaveReqVO.class).setType(2).setMode(1);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());

        // 调用
        sealApplyService.updateSealApply(reqVO, 10L);
        // 断言
        OaSealApplyDO updated = sealApplyMapper.selectById(apply.getId());
        assertEquals(0, apply.getContractPrice().compareTo(updated.getContractPrice()));
        assertEquals(apply.getContractParty(), updated.getContractParty());
        assertEquals(apply.getExpectedReturnTime(), updated.getExpectedReturnTime());
    }

    @Test
    public void testGetSealApplyPage_overdueUseStatus() {
        // mock 数据：已标逾期、外借超时、尚未超时各一条
        sealApplyMapper.insert(randomSealApplyDO().setStatus(2).setUseStatus(4));
        sealApplyMapper.insert(randomSealApplyDO().setStatus(2).setUseStatus(2)
                .setExpectedReturnTime(LocalDateTime.now().minusHours(1)));
        sealApplyMapper.insert(randomSealApplyDO().setStatus(2).setUseStatus(2));

        // 调用，并断言：按已保存状态筛选，不自动推导逾期
        assertEquals(1L, sealApplyService.getSealApplyPage(10L,
                new OaSealApplyPageReqVO().setUseStatus(4)).getTotal());
        assertEquals(2L, sealApplyService.getSealApplyPage(10L,
                new OaSealApplyPageReqVO().setUseStatus(2)).getTotal());
    }

    @Test
    public void testGetSealApplyPage_borrowedUseStatus() {
        // mock 数据
        sealApplyMapper.insert(randomSealApplyDO().setUseStatus(4));
        sealApplyMapper.insert(randomSealApplyDO().setUseStatus(2));
        // 准备参数
        OaSealApplyPageReqVO reqVO = new OaSealApplyPageReqVO().setUseStatus(2);

        // 调用及断言：只返回本人的外借状态申请，不包含逾期状态
        assertEquals(1L, sealApplyService.getSealApplyPage(10L, reqVO).getTotal());
    }

    @Test
    public void testGetSealApplyPage_useStatusNotSelected() {
        // mock 数据
        sealApplyMapper.insert(randomSealApplyDO().setUseStatus(4));
        sealApplyMapper.insert(randomSealApplyDO().setUseStatus(2));
        // 准备参数
        OaSealApplyPageReqVO reqVO = new OaSealApplyPageReqVO();

        // 调用及断言：未选择或清空用印状态时不限制状态
        assertEquals(2L, sealApplyService.getSealApplyPage(10L, reqVO).getTotal());
    }

    @Test
    public void testSubmitSealApply_overdueDoesNotExtendExpectedPeriod() {
        // mock 数据：历史逾期状态不把已经结束的预计时段无限延长
        sealApplyMapper.insert(randomSealApplyDO().setStatus(2).setUseStatus(4)
                .setExpectedUseTime(LocalDateTime.now().minusDays(2))
                .setExpectedReturnTime(LocalDateTime.now().minusDays(1)));
        OaSealApplyDO apply = randomSealApplyDO();
        sealApplyMapper.insert(apply);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("p1");

        // 调用，并断言：不增加逾期状态占用规则
        assertEquals("p1", sealApplyService.submitSealApply(apply.getId(), 10L));
    }

    @Test
    public void testSubmitSealApply_onsiteDoesNotOccupy() {
        // mock 数据
        OaSealApplyDO other = randomSealApplyDO().setStatus(1);
        OaSealApplyDO apply = randomSealApplyDO().setMode(1);
        sealApplyMapper.insert(other);
        sealApplyMapper.insert(apply);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("onsite");

        // 调用，并断言
        assertEquals("onsite", sealApplyService.submitSealApply(apply.getId(), 10L));
    }

    @Test
    public void testSubmitSealApply_pastTime() {
        // mock 数据
        OaSealApplyDO apply = randomSealApplyDO().setExpectedUseTime(LocalDateTime.now().minusDays(2))
                .setExpectedReturnTime(LocalDateTime.now().minusDays(1));
        sealApplyMapper.insert(apply);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("past");

        // 调用，并断言
        assertEquals("past", sealApplyService.submitSealApply(apply.getId(), 10L));
    }

    @Test
    public void testSubmitSealApply_borrowedBoundaryConflict() {
        // mock 数据：已借出的归还时刻与新申请用印时刻相同，闭区间仍重叠
        OaSealApplyDO other = randomSealApplyDO().setStatus(2).setUseStatus(2);
        OaSealApplyDO apply = randomSealApplyDO().setExpectedUseTime(other.getExpectedReturnTime())
                .setExpectedReturnTime(other.getExpectedReturnTime().plusHours(1));
        sealApplyMapper.insert(other);
        sealApplyMapper.insert(apply);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());

        // 调用，并断言异常
        assertServiceException(() -> sealApplyService.submitSealApply(apply.getId(), 10L), SEAL_APPLY_TIME_CONFLICT);
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testSubmitSealApply_approvedPendingOutsidePeriod() {
        // mock 数据：审批通过只占用预计时段，结束之后的新预约正常提交
        OaSealApplyDO other = randomSealApplyDO().setStatus(2);
        OaSealApplyDO apply = randomSealApplyDO().setExpectedUseTime(other.getExpectedReturnTime().plusHours(1))
                .setExpectedReturnTime(other.getExpectedReturnTime().plusHours(2));
        sealApplyMapper.insert(other);
        sealApplyMapper.insert(apply);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("outside");

        // 调用，并断言
        assertEquals("outside", sealApplyService.submitSealApply(apply.getId(), 10L));
    }

    @Test
    public void testUpdateSealApplyStatus_noConflictRecheckOrSealLookup() {
        // mock 数据：审批回调仅更新原申请，不重新检查台账及预约
        OaSealApplyDO apply = randomSealApplyDO().setStatus(1).setProcessInstanceId("p1");
        sealApplyMapper.insert(apply);
        sealApplyMapper.insert(randomSealApplyDO().setStatus(1));

        // 调用
        sealApplyService.updateSealApplyStatus(apply.getId(), 2);
        // 断言
        assertEquals(2, sealApplyMapper.selectById(apply.getId()).getStatus());
        verifyNoInteractions(sealService);
    }

    @Test
    public void testCanceledSealApply_stillReadOnly() {
        // mock 数据：撤回后不恢复草稿，保持 OA 请假的流程风格
        OaSealApplyDO apply = randomSealApplyDO().setStatus(4);
        sealApplyMapper.insert(apply);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());

        // 调用，并断言异常
        assertServiceException(() -> sealApplyService.updateSealApply(toBean(apply, OaSealApplySaveReqVO.class), 10L),
                SEAL_APPLY_STATUS_INVALID);
        assertServiceException(() -> sealApplyService.submitSealApply(apply.getId(), 10L), SEAL_APPLY_STATUS_INVALID);
        assertServiceException(() -> sealApplyService.deleteSealApply(apply.getId(), 10L), SEAL_APPLY_STATUS_INVALID);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    public void testCreateSealApply_fiveTypes(int type) {
        // 准备参数
        OaSealApplySaveReqVO reqVO = toBean(randomSealApplyDO(), OaSealApplySaveReqVO.class).setType(type);
        // mock 方法
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO());
        when(adminUserApi.getUser(10L)).thenReturn(new AdminUserRespDTO().setId(10L).setDeptId(20L));
        when(noRedisDAO.generate(OaNoRedisDAO.SEAL_APPLY_NO_PREFIX)).thenReturn("YY20260912000001");

        // 调用
        Long id = sealApplyService.createSealApply(reqVO, 10L);
        // 断言
        assertEquals(type, sealApplyMapper.selectById(id).getType());
    }

    @Test
    public void testCreateSealApply_noDuplicate() {
        // mock 数据
        OaSealApplyDO apply = randomSealApplyDO().setNo("YY20260912000001");
        sealApplyMapper.insert(apply);
        // 准备参数
        OaSealApplySaveReqVO reqVO = toBean(apply, OaSealApplySaveReqVO.class);
        // mock 方法
        when(sealService.validateSealExists(apply.getSealId())).thenReturn(randomSealDO());
        when(adminUserApi.getUser(10L)).thenReturn(new AdminUserRespDTO().setId(10L).setDeptId(20L));
        when(noRedisDAO.generate(OaNoRedisDAO.SEAL_APPLY_NO_PREFIX)).thenReturn(apply.getNo());

        // 调用，并断言异常
        assertServiceException(() -> sealApplyService.createSealApply(reqVO, 10L), SEAL_APPLY_NO_DUPLICATE);
    }

    @Test
    public void testSubmitSealApply_refreshSealSnapshot() {
        // mock 数据
        OaSealApplyDO apply = randomSealApplyDO().setSealType(1).setKeeperDeptId(20L);
        sealApplyMapper.insert(apply);
        // mock 方法：草稿保存后印章保管部门发生变化，提交时和原有快照一起刷新
        when(sealService.validateSealExists(30L)).thenReturn(randomSealDO().setType(2).setKeeperDeptId(21L));
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("seal-snapshot-process");

        // 调用
        sealApplyService.submitSealApply(apply.getId(), 10L);
        // 断言
        OaSealApplyDO result = sealApplyMapper.selectById(apply.getId());
        assertEquals(2, result.getSealType());
        assertEquals(21L, result.getKeeperDeptId());
    }

    @Test
    public void testGetSealApplyPage_deptAndCreateTime() {
        // mock 数据
        LocalDateTime createTime = LocalDateTime.of(2026, 9, 18, 10, 0);
        OaSealApplyDO matched = randomSealApplyDO();
        matched.setCreateTime(createTime);
        sealApplyMapper.insert(matched);
        OaSealApplyDO otherDept = randomSealApplyDO().setDeptId(21L);
        otherDept.setCreateTime(createTime);
        sealApplyMapper.insert(otherDept);
        OaSealApplyDO outsideTime = randomSealApplyDO();
        outsideTime.setCreateTime(createTime.minusDays(1));
        sealApplyMapper.insert(outsideTime);
        // 准备参数
        OaSealApplyPageReqVO reqVO = new OaSealApplyPageReqVO().setDeptId(20L)
                .setCreateTime(new LocalDateTime[]{createTime, createTime.plusHours(1)});

        // 调用
        PageResult<OaSealApplyDO> result = sealApplyService.getSealApplyPage(10L, reqVO);
        // 断言：包含起点，排除其他部门及范围外记录
        assertEquals(1L, result.getTotal());
        assertEquals(matched.getId(), CollUtil.getFirst(result.getList()).getId());
    }

    // ========== 随机对象 ==========

    /**
     * 构造尚未提交、未使用的用印申请
     *
     * @return 未入库的测试对象
     */
    private static OaSealApplyDO randomSealApplyDO() {
        return randomPojo(OaSealApplyDO.class, o -> o.setId(null).setUserId(10L).setDeptId(20L).setSealId(30L)
                .setKeeperUserId(40L).setSealType(1).setKeeperDeptId(20L).setType(1).setMode(2).setDocumentCount(1)
                .setContractPrice(BigDecimal.ONE)
                .setExpectedUseTime(LocalDateTime.now().plusDays(1).withNano(0))
                .setExpectedReturnTime(LocalDateTime.now().plusDays(1).plusHours(2).withNano(0))
                .setActualUseTime(null).setActualReturnTime(null).setStatus(-1).setUseStatus(0)
                .setProcessInstanceId(null).setFileUrls(Collections.emptyList()).setCreateTime(LocalDateTime.now().minusDays(1)));
    }

    /**
     * 构造具有指定保管人的可用印章
     *
     * @return 未入库的测试对象
     */
    private static OaSealDO randomSealDO() {
        return randomPojo(OaSealDO.class, o -> o.setId(30L).setType(1).setStatus(0).setKeeperUserId(40L).setKeeperDeptId(20L)
                .setSort(0));
    }
}
