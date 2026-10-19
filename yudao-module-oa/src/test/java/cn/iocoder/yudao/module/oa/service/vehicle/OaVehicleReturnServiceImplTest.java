package cn.iocoder.yudao.module.oa.service.vehicle;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.returning.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.*;
import cn.iocoder.yudao.module.oa.dal.mysql.vehicle.OaVehicleApplyMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.vehicle.OaVehicleMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.vehicle.OaVehicleReturnMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.service.vehicle.listener.OaVehicleReturnStatusListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.common.util.object.BeanUtils.toBean;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaVehicleReturnServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({OaVehicleReturnServiceImpl.class, OaVehicleReturnStatusListener.class})
public class OaVehicleReturnServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaVehicleReturnService vehicleReturnService;

    @Resource
    private OaVehicleReturnMapper vehicleReturnMapper;
    // 分页筛选验证关联查询，使用真实关联 Mapper 准备数据
    @Resource
    private OaVehicleApplyMapper vehicleApplyMapper;
    @Resource
    private OaVehicleMapper vehicleMapper;
    @Resource
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private OaNoRedisDAO noRedisDAO;
    @MockBean
    private OaVehicleApplyService vehicleApplyService;
    @MockBean
    private BpmProcessInstanceApi processInstanceApi;

    @Test
    public void testCreateVehicleReturn_actualTripDiffersFromPlan() {
        // mock 数据：实际提前出车，不能用计划时间代替事实
        OaVehicleApplyDO apply = randomVehicleApplyDO();
        OaVehicleReturnSaveReqVO reqVO = toBean(randomVehicleReturnDO(), OaVehicleReturnSaveReqVO.class)
                .setActualStartTime(apply.getStartTime().minusHours(2)).setActualReturnTime(apply.getStartTime().minusHours(1))
                .setStartLocation("客户现场").setReason("现场支持").setPassenger("李四");
        // mock 方法
        when(vehicleApplyService.validateVehicleApplyExists(20L)).thenReturn(apply);
        when(noRedisDAO.generate(OaNoRedisDAO.VEHICLE_RETURN_NO_PREFIX)).thenReturn("HC202609170001");

        // 调用
        Long id = vehicleReturnService.createVehicleReturn(reqVO, 10L);
        // 断言
        OaVehicleReturnDO result = vehicleReturnMapper.selectById(id);
        assertEquals(reqVO.getActualStartTime(), result.getActualStartTime());
        assertEquals("客户现场", result.getStartLocation());
        assertEquals("现场支持", result.getReason());
        assertEquals("李四", result.getPassenger());
    }

    @Test
    public void testGetVehicleReturnPage_filters() {
        // mock 数据
        LocalDateTime createTime = LocalDateTime.of(2026, 9, 12, 12, 0);
        OaVehicleDO vehicle = new OaVehicleDO().setId(30L).setNo("沪A12345").setName("商务车").setType("商务车")
                .setCategory("商务接待用车").setSeatCount(7).setStatus(0).setSort(0);
        vehicleMapper.insert(vehicle);
        OaVehicleApplyDO apply = randomVehicleApplyDO().setNo("YC202609120001");
        vehicleApplyMapper.insert(apply);
        OaVehicleReturnDO record = randomVehicleReturnDO().setNo("HC202609120001");
        record.setCreateTime(createTime);
        vehicleReturnMapper.insert(record);
        // 准备参数
        OaVehicleReturnPageReqVO reqVO = new OaVehicleReturnPageReqVO().setNo("HC20260912")
                .setVehicleNo("12345").setApplyNo("YC20260912").setDeptId(40L)
                .setCreateTime(new LocalDateTime[]{createTime, createTime});

        // 调用，并断言：关联筛选命中同一条还车记录，不扩大本人范围
        assertEquals(1, vehicleReturnService.getVehicleReturnPage(10L, reqVO).getTotal());
        assertEquals(record.getId(), CollUtil.getFirst(vehicleReturnService.getVehicleReturnPage(10L, reqVO).getList()).getId());
        assertEquals(0, vehicleReturnService.getVehicleReturnPage(11L, reqVO).getTotal());
        reqVO.setVehicleNo("99999");
        assertEquals(0, vehicleReturnService.getVehicleReturnPage(10L, reqVO).getTotal());
        reqVO.setVehicleNo("12345").setApplyNo("not-exists");
        assertEquals(0, vehicleReturnService.getVehicleReturnPage(10L, reqVO).getTotal());
        reqVO.setApplyNo("YC20260912").setDeptId(41L);
        assertEquals(0, vehicleReturnService.getVehicleReturnPage(10L, reqVO).getTotal());
        reqVO.setDeptId(40L).setCreateTime(new LocalDateTime[]{createTime.plusSeconds(1), createTime.plusDays(1)});
        assertEquals(0, vehicleReturnService.getVehicleReturnPage(10L, reqVO).getTotal());
        // 未填写关联筛选时，无关联数据的历史记录仍可查询
        vehicleReturnMapper.insert(randomVehicleReturnDO().setApplyId(999L).setVehicleId(999L));
        assertEquals(2, vehicleReturnService.getVehicleReturnPage(10L, new OaVehicleReturnPageReqVO()).getTotal());
    }

    @Test
    public void testGetVehicleReturn_success() {
        // mock 数据
        OaVehicleReturnDO record = randomVehicleReturnDO().setProcessInstanceId("detail-process");
        vehicleReturnMapper.insert(record);

        // 调用，并断言：详情查询不依赖审批参与关系
        assertEquals(record.getId(), vehicleReturnService.getVehicleReturn(record.getId()).getId());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testGetVehicleReturn_notExists() {

        // 调用，并断言
        assertNull(vehicleReturnService.getVehicleReturn(-1L));
    }

    @Test
    public void testUpdateVehicleReturn_clearFiles() {
        // mock 数据
        OaVehicleReturnDO record = randomVehicleReturnDO()
                .setFileUrls(Arrays.asList("https://example.com/a.pdf", "https://example.com/b.docx"));
        vehicleReturnMapper.insert(record);
        // 准备参数
        OaVehicleReturnSaveReqVO reqVO = toBean(record, OaVehicleReturnSaveReqVO.class)
                .setFileUrls(Collections.emptyList());
        // mock 方法
        when(vehicleApplyService.validateVehicleApplyExists(20L)).thenReturn(randomVehicleApplyDO());

        // 调用
        vehicleReturnService.updateVehicleReturn(reqVO, 10L);

        // 断言
        assertEquals(Collections.emptyList(), vehicleReturnMapper.selectById(record.getId()).getFileUrls());
    }

    @Test
    public void testCreateVehicleReturn() {
        // mock 单号生成
        when(noRedisDAO.generate(OaNoRedisDAO.VEHICLE_RETURN_NO_PREFIX)).thenReturn("VEHICLE_RETURN20260912000001");
        // 准备参数
        OaVehicleReturnSaveReqVO reqVO = toBean(randomVehicleReturnDO(), OaVehicleReturnSaveReqVO.class);
        reqVO.setFileUrls(Arrays.asList("https://example.com/a.pdf", "https://example.com/b.docx"));
        // mock 方法
        when(vehicleApplyService.validateVehicleApplyExists(20L)).thenReturn(randomVehicleApplyDO());

        // 调用
        Long id = vehicleReturnService.createVehicleReturn(reqVO, 10L);

        // 断言
        OaVehicleReturnDO actual = vehicleReturnMapper.selectById(id);
        assertEquals("VEHICLE_RETURN20260912000001", actual.getNo());
        assertEquals(30L, actual.getVehicleId());
        assertEquals(10L, actual.getUserId());
        assertEquals(40L, actual.getDeptId());
        assertEquals(-1, actual.getStatus());
        assertNull(actual.getProcessInstanceId());
        assertEquals(reqVO.getFileUrls(), actual.getFileUrls());
        verify(vehicleApplyService, never()).updateVehicleApplyReturnStatus(any(), any(), any());
    }

    @Test
    public void testCreateVehicleReturn_noDuplicate() {
        // mock 单号生成
        when(noRedisDAO.generate(OaNoRedisDAO.VEHICLE_RETURN_NO_PREFIX)).thenReturn("HC12345678");
        // mock 数据
        OaVehicleReturnDO other = randomVehicleReturnDO().setApplyId(21L).setNo("HC12345678");
        vehicleReturnMapper.insert(other);
        // 准备参数
        OaVehicleReturnSaveReqVO reqVO = toBean(randomVehicleReturnDO(), OaVehicleReturnSaveReqVO.class);
        // mock 方法
        when(vehicleApplyService.validateVehicleApplyExists(20L)).thenReturn(randomVehicleApplyDO());

        // 调用，并断言异常
        assertServiceException(() -> vehicleReturnService.createVehicleReturn(reqVO, 10L), VEHICLE_RETURN_NO_DUPLICATE);
    }

    @Test
    public void testUpdateVehicleReturn_preserveNo() {
        // mock 数据
        OaVehicleReturnDO vehicleReturn = randomVehicleReturnDO().setNo("HC12345678");
        vehicleReturnMapper.insert(vehicleReturn);
        OaVehicleReturnDO other = randomVehicleReturnDO().setApplyId(21L).setNo("HC87654321");
        vehicleReturnMapper.insert(other);
        // 准备参数
        OaVehicleReturnSaveReqVO reqVO = toBean(vehicleReturn, OaVehicleReturnSaveReqVO.class);
        // mock 方法
        when(vehicleApplyService.validateVehicleApplyExists(20L)).thenReturn(randomVehicleApplyDO());

        // 调用，并断言原单号不变
        vehicleReturnService.updateVehicleReturn(reqVO, 10L);
        verifyNoInteractions(noRedisDAO);
        assertEquals(vehicleReturn.getNo(), vehicleReturnMapper.selectById(vehicleReturn.getId()).getNo());
    }

    @Test
    public void testCreateVehicleReturn_duplicateDraft() {
        // mock 单号生成
        when(noRedisDAO.generate(OaNoRedisDAO.VEHICLE_RETURN_NO_PREFIX)).thenReturn("VEHICLE_RETURN20260912000001");
        // mock 数据
        vehicleReturnMapper.insert(randomVehicleReturnDO());
        // mock 方法
        when(vehicleApplyService.validateVehicleApplyExists(20L)).thenReturn(randomVehicleApplyDO());
        // 准备参数
        OaVehicleReturnSaveReqVO reqVO = toBean(randomVehicleReturnDO(), OaVehicleReturnSaveReqVO.class);

        // 调用
        Long id = vehicleReturnService.createVehicleReturn(reqVO, 10L);
        // 断言
        assertNotNull(id);
        assertEquals(2L, vehicleReturnMapper.selectCount());
    }

    @Test
    public void testCreateVehicleReturn_afterRejected() {
        // mock 单号生成
        when(noRedisDAO.generate(OaNoRedisDAO.VEHICLE_RETURN_NO_PREFIX)).thenReturn("VEHICLE_RETURN20260912000001");
        // mock 数据
        vehicleReturnMapper.insert(randomVehicleReturnDO().setStatus(3));
        vehicleReturnMapper.insert(randomVehicleReturnDO().setStatus(4));
        // mock 方法
        when(vehicleApplyService.validateVehicleApplyExists(20L)).thenReturn(randomVehicleApplyDO());
        // 准备参数
        OaVehicleReturnSaveReqVO reqVO = toBean(randomVehicleReturnDO(), OaVehicleReturnSaveReqVO.class);

        // 调用，并断言
        assertNotNull(vehicleReturnService.createVehicleReturn(reqVO, 10L));
    }

    @Test
    public void testCreateVehicleReturn_beforeDeparture() {
        // mock 单号生成
        when(noRedisDAO.generate(OaNoRedisDAO.VEHICLE_RETURN_NO_PREFIX)).thenReturn("VEHICLE_RETURN20260912000001");
        // mock 方法
        OaVehicleApplyDO apply = randomVehicleApplyDO();
        when(vehicleApplyService.validateVehicleApplyExists(20L)).thenReturn(apply);
        // 准备参数
        OaVehicleReturnSaveReqVO reqVO = toBean(randomVehicleReturnDO(), OaVehicleReturnSaveReqVO.class)
                .setActualReturnTime(apply.getStartTime().minusHours(1));

        // 调用，并断言异常
        assertServiceException(() -> vehicleReturnService.createVehicleReturn(reqVO, 10L), VEHICLE_RETURN_TIME_INVALID);
    }

    @Test
    public void testCreateVehicleReturn_notApproved() {
        // mock 单号生成
        when(noRedisDAO.generate(OaNoRedisDAO.VEHICLE_RETURN_NO_PREFIX)).thenReturn("VEHICLE_RETURN20260912000001");
        // mock 方法
        when(vehicleApplyService.validateVehicleApplyExists(20L)).thenReturn(randomVehicleApplyDO().setStatus(1));
        // 准备参数
        OaVehicleReturnSaveReqVO reqVO = toBean(randomVehicleReturnDO(), OaVehicleReturnSaveReqVO.class);

        // 调用，并断言异常
        assertServiceException(() -> vehicleReturnService.createVehicleReturn(reqVO, 10L), VEHICLE_RETURN_STATUS_INVALID);
    }

    @Test
    public void testSubmitVehicleReturn_duplicate() {
        // mock 数据
        OaVehicleReturnDO vehicleReturn = randomVehicleReturnDO();
        vehicleReturnMapper.insert(vehicleReturn);
        // mock 方法
        when(vehicleApplyService.validateVehicleApplyExists(20L)).thenReturn(randomVehicleApplyDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenAnswer(invocation -> {
            // BPM 创建流程时会追加系统变量，传入的变量 Map 必须可修改
            BpmProcessInstanceCreateReqDTO reqDTO = invocation.getArgument(1);
            reqDTO.getVariables().put("START_USER_ID", 10L);
            return "return-process";
        });

        // 调用
        assertEquals("return-process", vehicleReturnService.submitVehicleReturn(vehicleReturn.getId(), 10L));
        assertServiceException(() -> vehicleReturnService.submitVehicleReturn(vehicleReturn.getId(), 10L),
                VEHICLE_RETURN_STATUS_INVALID);

        // 断言
        assertEquals(1, vehicleReturnMapper.selectById(vehicleReturn.getId()).getStatus());
        verify(vehicleApplyService, times(1)).updateVehicleApplyReturnStatus(20L, 1, 2);
        verify(processInstanceApi, times(1)).createProcessInstance(eq(10L), any());
    }

    @Test
    public void testSubmitVehicleReturn_processFailureRollback() {
        // mock 数据
        OaVehicleReturnDO vehicleReturn = randomVehicleReturnDO();
        vehicleReturnMapper.insert(vehicleReturn);
        // mock 方法
        when(vehicleApplyService.validateVehicleApplyExists(20L)).thenReturn(randomVehicleApplyDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenThrow(new IllegalStateException("模型未发布"));

        // 调用
        assertThrows(IllegalStateException.class, () -> vehicleReturnService.submitVehicleReturn(vehicleReturn.getId(), 10L));

        // 断言
        assertEquals(-1, vehicleReturnMapper.selectById(vehicleReturn.getId()).getStatus());
        assertNull(vehicleReturnMapper.selectById(vehicleReturn.getId()).getProcessInstanceId());
    }

    @Test
    public void testUpdateVehicleReturnStatus_approvedAndIdempotent() {
        // mock 数据
        OaVehicleReturnDO vehicleReturn = randomVehicleReturnDO().setStatus(1).setProcessInstanceId("approved");
        vehicleReturnMapper.insert(vehicleReturn);

        // 调用
        vehicleReturnService.updateVehicleReturnStatus(vehicleReturn.getId(), 2);
        vehicleReturnService.updateVehicleReturnStatus(vehicleReturn.getId(), 2);
        vehicleReturnService.updateVehicleReturnStatus(vehicleReturn.getId(), 3);

        // 断言
        assertEquals(2, vehicleReturnMapper.selectById(vehicleReturn.getId()).getStatus());
        assertEquals("approved", vehicleReturnMapper.selectById(vehicleReturn.getId()).getProcessInstanceId());
        verify(vehicleApplyService, times(1)).updateVehicleApplyReturnStatus(20L, 2, 3);
        verifyNoMoreInteractions(vehicleApplyService);
    }

    @Test
    public void testUpdateVehicleReturnStatus_applyUpdateFailure() {
        // mock 数据
        OaVehicleReturnDO vehicleReturn = randomVehicleReturnDO().setStatus(1).setProcessInstanceId("approved");
        vehicleReturnMapper.insert(vehicleReturn);
        // mock 方法
        doThrow(new IllegalStateException("原用车单更新失败")).when(vehicleApplyService)
                .updateVehicleApplyReturnStatus(20L, 2, 3);

        // 调用，并断言
        assertThrows(IllegalStateException.class,
                () -> vehicleReturnService.updateVehicleReturnStatus(vehicleReturn.getId(), 2));
        assertEquals(1, vehicleReturnMapper.selectById(vehicleReturn.getId()).getStatus());
    }

    @Test
    public void testUpdateVehicleReturnStatus_rejected() {
        // mock 数据
        OaVehicleReturnDO vehicleReturn = randomVehicleReturnDO().setStatus(1).setProcessInstanceId("rejected");
        vehicleReturnMapper.insert(vehicleReturn);

        // 调用
        vehicleReturnService.updateVehicleReturnStatus(vehicleReturn.getId(), 3);

        // 断言
        verify(vehicleApplyService).updateVehicleApplyReturnStatus(20L, 2, 1);
        assertEquals(3, vehicleReturnMapper.selectById(vehicleReturn.getId()).getStatus());
    }

    @Test
    public void testSubmitVehicleReturn_synchronousApproval() {
        // mock 数据
        OaVehicleReturnDO vehicleReturn = randomVehicleReturnDO();
        vehicleReturnMapper.insert(vehicleReturn);
        // mock 方法
        when(vehicleApplyService.validateVehicleApplyExists(20L)).thenReturn(randomVehicleApplyDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenAnswer(invocation -> {
            eventPublisher.publishEvent(new BpmProcessInstanceStatusEvent(this).setId("auto")
                    .setProcessDefinitionKey(BpmModelConstants.VEHICLE_RETURN)
                    .setBusinessKey(vehicleReturn.getId().toString()).setStatus(2));
            return "auto";
        });

        // 调用
        vehicleReturnService.submitVehicleReturn(vehicleReturn.getId(), 10L);

        // 断言
        assertEquals(2, vehicleReturnMapper.selectById(vehicleReturn.getId()).getStatus());
        assertEquals("auto", vehicleReturnMapper.selectById(vehicleReturn.getId()).getProcessInstanceId());
    }

    @Test
    public void testUpdateVehicleReturn_changeApply() {
        // mock 数据
        OaVehicleReturnDO vehicleReturn = randomVehicleReturnDO();
        vehicleReturnMapper.insert(vehicleReturn);
        // 准备参数
        OaVehicleReturnSaveReqVO reqVO = toBean(vehicleReturn, OaVehicleReturnSaveReqVO.class).setApplyId(21L);
        // mock 方法
        when(vehicleApplyService.validateVehicleApplyExists(21L)).thenReturn(randomVehicleApplyDO()
                .setId(21L).setVehicleId(31L).setDeptId(41L));

        // 调用
        vehicleReturnService.updateVehicleReturn(reqVO, 10L);

        // 断言
        OaVehicleReturnDO actual = vehicleReturnMapper.selectById(vehicleReturn.getId());
        assertEquals(21L, actual.getApplyId());
        assertEquals(31L, actual.getVehicleId());
        assertEquals(41L, actual.getDeptId());
        assertEquals(10L, actual.getUserId());
    }

    @Test
    public void testGetVehicleReturn_ownerScope() {
        // mock 数据
        OaVehicleReturnDO vehicleReturn = randomVehicleReturnDO().setUserId(11L);
        vehicleReturnMapper.insert(vehicleReturn);

        // 调用，并断言
        assertEquals(0, vehicleReturnService.getVehicleReturnPage(10L, new OaVehicleReturnPageReqVO()).getTotal());
        assertEquals(vehicleReturn.getId(), vehicleReturnService.getVehicleReturn(vehicleReturn.getId()).getId());
        assertServiceException(() -> vehicleReturnService.deleteVehicleReturn(vehicleReturn.getId(), 10L),
                VEHICLE_RETURN_NOT_OWNER);
    }

    @Test
    public void testCancelVehicleReturn() {
        // mock 数据
        OaVehicleReturnDO vehicleReturn = randomVehicleReturnDO().setStatus(1).setProcessInstanceId("cancel");
        vehicleReturnMapper.insert(vehicleReturn);
        // mock 方法
        doAnswer(invocation -> {
            eventPublisher.publishEvent(new BpmProcessInstanceStatusEvent(this).setId("cancel")
                    .setProcessDefinitionKey(BpmModelConstants.VEHICLE_RETURN)
                    .setBusinessKey(vehicleReturn.getId().toString()).setStatus(4));
            return null;
        }).when(processInstanceApi).cancelProcessInstanceByStartUser(10L, "cancel", "申请人取消还车申请");

        // 调用
        vehicleReturnService.cancelVehicleReturn(vehicleReturn.getId(), 10L);

        // 断言
        assertEquals(4, vehicleReturnMapper.selectById(vehicleReturn.getId()).getStatus());
        verify(vehicleApplyService).updateVehicleApplyReturnStatus(20L, 2, 1);
    }

    @Test
    public void testDeleteVehicleReturn_onlyDraft() {
        // mock 数据
        OaVehicleReturnDO draft = randomVehicleReturnDO();
        vehicleReturnMapper.insert(draft);
        OaVehicleReturnDO running = randomVehicleReturnDO().setStatus(1);
        vehicleReturnMapper.insert(running);

        // 调用
        vehicleReturnService.deleteVehicleReturn(draft.getId(), 10L);

        // 断言
        assertNull(vehicleReturnMapper.selectById(draft.getId()));
        assertServiceException(() -> vehicleReturnService.deleteVehicleReturn(running.getId(), 10L),
                VEHICLE_RETURN_STATUS_INVALID);
    }

    @Test
    public void testCreateVehicleReturn_futureTime() {
        // mock 单号生成
        when(noRedisDAO.generate(OaNoRedisDAO.VEHICLE_RETURN_NO_PREFIX)).thenReturn("VEHICLE_RETURN20260912000001");
        // mock 方法
        when(vehicleApplyService.validateVehicleApplyExists(20L)).thenReturn(randomVehicleApplyDO());
        // 准备参数
        OaVehicleReturnSaveReqVO reqVO = toBean(randomVehicleReturnDO(), OaVehicleReturnSaveReqVO.class)
                .setActualReturnTime(LocalDateTime.now().plusHours(1));

        // 调用，并断言异常
        assertServiceException(() -> vehicleReturnService.createVehicleReturn(reqVO, 10L), VEHICLE_RETURN_TIME_INVALID);
    }

    @Test
    public void testUpdateVehicleReturn_changeApplyNotOwner() {
        // mock 数据
        OaVehicleReturnDO vehicleReturn = randomVehicleReturnDO();
        vehicleReturnMapper.insert(vehicleReturn);
        // 准备参数
        OaVehicleReturnSaveReqVO reqVO = toBean(vehicleReturn, OaVehicleReturnSaveReqVO.class).setApplyId(21L);
        // mock 方法
        when(vehicleApplyService.validateVehicleApplyExists(21L)).thenReturn(randomVehicleApplyDO().setId(21L).setUserId(11L));

        // 调用，并断言异常
        assertServiceException(() -> vehicleReturnService.updateVehicleReturn(reqVO, 10L), VEHICLE_RETURN_NOT_OWNER);
        assertEquals(20L, vehicleReturnMapper.selectById(vehicleReturn.getId()).getApplyId());
    }

    @Test
    public void testUpdateVehicleReturn_nullDeptIgnored() {
        // mock 数据
        OaVehicleReturnDO vehicleReturn = randomVehicleReturnDO();
        vehicleReturnMapper.insert(vehicleReturn);
        // 准备参数
        OaVehicleReturnSaveReqVO reqVO = toBean(vehicleReturn, OaVehicleReturnSaveReqVO.class).setApplyId(21L);
        // mock 方法
        when(vehicleApplyService.validateVehicleApplyExists(21L)).thenReturn(randomVehicleApplyDO().setId(21L).setDeptId(null));

        // 调用
        vehicleReturnService.updateVehicleReturn(reqVO, 10L);

        // 断言：空部门遵循 updateById 默认忽略策略
        OaVehicleReturnDO actual = vehicleReturnMapper.selectById(vehicleReturn.getId());
        assertEquals(21L, actual.getApplyId());
        assertEquals(vehicleReturn.getDeptId(), actual.getDeptId());
    }

    @Test
    public void testSubmitVehicleReturn_multipleDrafts() {
        // mock 数据
        OaVehicleReturnDO first = randomVehicleReturnDO();
        vehicleReturnMapper.insert(first);
        OaVehicleReturnDO second = randomVehicleReturnDO();
        vehicleReturnMapper.insert(second);
        // mock 方法：第一张提交后，原用车单进入还车中
        OaVehicleApplyDO apply = randomVehicleApplyDO();
        when(vehicleApplyService.validateVehicleApplyExists(20L)).thenReturn(apply);
        doAnswer(invocation -> {
            apply.setReturnStatus(2);
            return null;
        }).when(vehicleApplyService).updateVehicleApplyReturnStatus(20L, 1, 2);
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("first-return");

        // 调用
        vehicleReturnService.submitVehicleReturn(first.getId(), 10L);

        // 断言：另一草稿不能再次发起，还车流程只有一个
        assertServiceException(() -> vehicleReturnService.submitVehicleReturn(second.getId(), 10L),
                VEHICLE_RETURN_STATUS_INVALID);
        assertEquals(-1, vehicleReturnMapper.selectById(second.getId()).getStatus());
        verify(processInstanceApi, times(1)).createProcessInstance(eq(10L), any());
    }

    // ========== 随机对象 ==========

    /**
     * 构造尚未提交的还车申请。
     *
     * @return 未入库的测试对象
     */
    private static OaVehicleReturnDO randomVehicleReturnDO() {
        return randomPojo(OaVehicleReturnDO.class, record -> record.setId(null).setApplyId(20L)
                .setVehicleId(30L).setUserId(10L).setDeptId(40L)
                .setActualStartTime(LocalDateTime.now().minusHours(2))
                .setActualReturnTime(LocalDateTime.now().minusHours(1))
                .setStatus(-1).setProcessInstanceId(null));
    }

    /**
     * 构造具有合法用车时段和固定业务关联的用车申请。
     *
     * @return 未入库的测试对象
     */
    private static OaVehicleApplyDO randomVehicleApplyDO() {
        return randomPojo(OaVehicleApplyDO.class, apply -> apply.setId(20L)
                .setVehicleId(30L).setUserId(10L).setDeptId(40L)
                .setStartTime(LocalDateTime.now().minusHours(3)).setEndTime(LocalDateTime.now())
                .setStatus(2).setReturnStatus(1));
    }

}
