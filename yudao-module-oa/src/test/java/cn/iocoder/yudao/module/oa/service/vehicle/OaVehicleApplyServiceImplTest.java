package cn.iocoder.yudao.module.oa.service.vehicle;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.apply.OaVehicleApplyPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.apply.OaVehicleApplySaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleApplyDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleDO;
import cn.iocoder.yudao.module.oa.dal.mysql.vehicle.OaVehicleApplyMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.enums.vehicle.OaVehicleStatusEnum;
import cn.iocoder.yudao.module.oa.service.vehicle.listener.OaVehicleApplyStatusListener;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
 * {@link OaVehicleApplyServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({OaVehicleApplyServiceImpl.class, OaVehicleApplyStatusListener.class})
public class OaVehicleApplyServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaVehicleApplyService vehicleApplyService;

    @Resource
    private OaVehicleApplyMapper vehicleApplyMapper;
    @Resource
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private OaNoRedisDAO noRedisDAO;
    @MockBean
    private OaVehicleService vehicleService;
    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private BpmProcessInstanceApi processInstanceApi;

    @Test
    public void testGetVehicleApply_success() {
        // mock 数据
        OaVehicleApplyDO record = randomVehicleApplyDO().setProcessInstanceId("detail-process");
        vehicleApplyMapper.insert(record);

        // 调用，并断言：详情查询不依赖审批参与关系
        assertEquals(record.getId(), vehicleApplyService.getVehicleApply(record.getId()).getId());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testGetVehicleApply_notExists() {

        // 调用，并断言
        assertNull(vehicleApplyService.getVehicleApply(-1L));
    }

    @Test
    public void testSubmitVehicleApply_notExists() {

        // 调用，并断言异常
        assertServiceException(() -> vehicleApplyService.submitVehicleApply(-1L, 1L), VEHICLE_APPLY_NOT_EXISTS);
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testUpdateVehicleApply_clearFiles() {
        // mock 数据
        OaVehicleApplyDO record = randomVehicleApplyDO()
                .setFileUrls(Arrays.asList("https://example.com/a.pdf", "https://example.com/b.docx"));
        vehicleApplyMapper.insert(record);
        // 准备参数
        OaVehicleApplySaveReqVO reqVO = toBean(record, OaVehicleApplySaveReqVO.class)
                .setFileUrls(Collections.emptyList());
        // mock 方法
        when(vehicleService.validateVehicleExists(30L)).thenReturn(randomVehicleDO());
        when(vehicleService.validateVehicleExists(30L)).thenReturn(randomVehicleDO());

        // 调用
        vehicleApplyService.updateVehicleApply(reqVO, 10L);

        // 断言
        assertEquals(Collections.emptyList(), vehicleApplyMapper.selectById(record.getId()).getFileUrls());
    }

    @Test
    public void testCreateVehicleApply_noDuplicate() {
        // mock 单号生成
        when(noRedisDAO.generate(OaNoRedisDAO.VEHICLE_APPLY_NO_PREFIX)).thenReturn("YC12345678");
        // mock 数据
        OaVehicleApplyDO apply = randomVehicleApplyDO().setNo("YC12345678");
        vehicleApplyMapper.insert(apply);
        // 准备参数
        OaVehicleApplySaveReqVO reqVO = toBean(randomVehicleApplyDO(), OaVehicleApplySaveReqVO.class);
        // mock 方法
        when(vehicleService.validateVehicleExists(30L)).thenReturn(randomVehicleDO());
        when(adminUserApi.validateUser(10L)).thenReturn(new AdminUserRespDTO().setId(10L).setDeptId(20L));

        // 调用，并断言异常
        assertServiceException(() -> vehicleApplyService.createVehicleApply(reqVO, 10L), VEHICLE_APPLY_NO_DUPLICATE);
    }

    @Test
    public void testUpdateVehicleApply_preserveNo() {
        // mock 数据
        OaVehicleApplyDO apply = randomVehicleApplyDO().setNo("YC12345678");
        vehicleApplyMapper.insert(apply);
        OaVehicleApplyDO other = randomVehicleApplyDO().setNo("YC87654321");
        vehicleApplyMapper.insert(other);
        // 准备参数
        OaVehicleApplySaveReqVO reqVO = toBean(apply, OaVehicleApplySaveReqVO.class);
        // mock 方法
        when(vehicleService.validateVehicleExists(30L)).thenReturn(randomVehicleDO());

        // 调用，并断言原单号不变
        vehicleApplyService.updateVehicleApply(reqVO, 10L);
        verifyNoInteractions(noRedisDAO);
        assertEquals(apply.getNo(), vehicleApplyMapper.selectById(apply.getId()).getNo());
    }

    @Test
    public void testCreateVehicleApply() {
        // mock 单号生成
        when(noRedisDAO.generate(OaNoRedisDAO.VEHICLE_APPLY_NO_PREFIX)).thenReturn("VEHICLE_APPLY20260912000001");
        // 准备参数
        OaVehicleApplySaveReqVO reqVO = toBean(randomVehicleApplyDO(), OaVehicleApplySaveReqVO.class);
        reqVO.setId(999L);
        reqVO.setFileUrls(Arrays.asList("https://example.com/a.pdf", "https://example.com/b.docx"));
        // mock 方法
        when(vehicleService.validateVehicleExists(30L)).thenReturn(randomVehicleDO());
        when(adminUserApi.validateUser(10L)).thenReturn(new AdminUserRespDTO().setId(10L).setDeptId(20L));

        // 调用
        Long id = vehicleApplyService.createVehicleApply(reqVO, 10L);

        // 断言
        OaVehicleApplyDO apply = vehicleApplyMapper.selectById(id);
        assertNotEquals(999L, id);
        assertEquals("VEHICLE_APPLY20260912000001", apply.getNo());
        assertEquals(10L, apply.getUserId());
        assertEquals(20L, apply.getDeptId());
        assertEquals(-1, apply.getStatus());
        assertEquals(0, apply.getReturnStatus());
        assertNull(apply.getProcessInstanceId());
        assertEquals(reqVO.getFileUrls(), apply.getFileUrls());
    }

    @Test
    public void testSubmitVehicleApply_duplicate() {
        // mock 数据
        OaVehicleApplyDO apply = randomVehicleApplyDO().setPassenger("测试随行人");
        vehicleApplyMapper.insert(apply);
        // mock 方法
        when(vehicleService.validateVehicleExists(30L)).thenReturn(randomVehicleDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenAnswer(invocation -> {
            // BPM 创建流程时会追加系统变量，传入的变量 Map 必须可修改
            BpmProcessInstanceCreateReqDTO reqDTO = invocation.getArgument(1);
            reqDTO.getVariables().put("START_USER_ID", 10L);
            return "process-1";
        });

        // 调用
        assertEquals("process-1", vehicleApplyService.submitVehicleApply(apply.getId(), 10L));
        assertServiceException(() -> vehicleApplyService.submitVehicleApply(apply.getId(), 10L), VEHICLE_APPLY_STATUS_INVALID);

        // 断言
        OaVehicleApplyDO actual = vehicleApplyMapper.selectById(apply.getId());
        assertEquals(1, actual.getStatus());
        assertEquals("测试随行人", actual.getPassenger());
        verify(processInstanceApi, times(1)).createProcessInstance(eq(10L), any());
    }

    @Test
    public void testSubmitVehicleApply_overlap() {
        // mock 数据
        OaVehicleApplyDO occupied = randomVehicleApplyDO().setStatus(1);
        vehicleApplyMapper.insert(occupied);
        OaVehicleApplyDO apply = randomVehicleApplyDO();
        vehicleApplyMapper.insert(apply);
        // mock 方法
        when(vehicleService.validateVehicleExists(30L)).thenReturn(randomVehicleDO());

        // 调用，并断言异常
        assertServiceException(() -> vehicleApplyService.submitVehicleApply(apply.getId(), 10L),
                VEHICLE_APPLY_TIME_CONFLICT);
        verifyNoInteractions(processInstanceApi);
        assertEquals(-1, vehicleApplyMapper.selectById(apply.getId()).getStatus());
    }

    @Test
    public void testSubmitVehicleApply_adjacentTime() {
        // mock 数据
        OaVehicleApplyDO occupied = randomVehicleApplyDO().setStatus(2).setReturnStatus(1);
        vehicleApplyMapper.insert(occupied);
        OaVehicleApplyDO apply = randomVehicleApplyDO().setStartTime(occupied.getEndTime())
                .setEndTime(occupied.getEndTime().plusHours(1));
        vehicleApplyMapper.insert(apply);
        // mock 方法
        when(vehicleService.validateVehicleExists(30L)).thenReturn(randomVehicleDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("adjacent");

        // 调用，并断言
        assertEquals("adjacent", vehicleApplyService.submitVehicleApply(apply.getId(), 10L));
    }

    @Test
    public void testSubmitVehicleApply_rejectedDoesNotOccupy() {
        // mock 数据
        vehicleApplyMapper.insert(randomVehicleApplyDO().setStatus(3));
        vehicleApplyMapper.insert(randomVehicleApplyDO().setStatus(4));
        vehicleApplyMapper.insert(randomVehicleApplyDO().setStatus(2).setReturnStatus(3));
        OaVehicleApplyDO apply = randomVehicleApplyDO();
        vehicleApplyMapper.insert(apply);
        // mock 方法
        when(vehicleService.validateVehicleExists(30L)).thenReturn(randomVehicleDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("released");

        // 调用，并断言
        assertEquals("released", vehicleApplyService.submitVehicleApply(apply.getId(), 10L));
    }

    @Test
    public void testSubmitVehicleApply_processFailureRollback() {
        // mock 数据
        OaVehicleApplyDO apply = randomVehicleApplyDO();
        vehicleApplyMapper.insert(apply);
        // mock 方法
        when(vehicleService.validateVehicleExists(30L)).thenReturn(randomVehicleDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenThrow(new IllegalStateException("未发布模型"));

        // 调用
        assertThrows(IllegalStateException.class, () -> vehicleApplyService.submitVehicleApply(apply.getId(), 10L));

        // 断言
        assertEquals(-1, vehicleApplyMapper.selectById(apply.getId()).getStatus());
        assertNull(vehicleApplyMapper.selectById(apply.getId()).getProcessInstanceId());
    }

    @ParameterizedTest
    @EnumSource(OaVehicleStatusEnum.class)
    public void testCreateVehicleApply_vehicleStatus(OaVehicleStatusEnum status) {
        // 准备参数
        OaVehicleApplySaveReqVO reqVO = toBean(randomVehicleApplyDO(), OaVehicleApplySaveReqVO.class);
        // mock 方法
        when(vehicleService.validateVehicleExists(30L)).thenReturn(randomVehicleDO().setStatus(status.getStatus()));
        when(adminUserApi.validateUser(10L)).thenReturn(new AdminUserRespDTO().setId(10L).setDeptId(20L));
        when(noRedisDAO.generate(OaNoRedisDAO.VEHICLE_APPLY_NO_PREFIX)).thenReturn("YC202609120001");

        // 调用
        Long id = vehicleApplyService.createVehicleApply(reqVO, 10L);

        // 断言：草稿允许选择任意状态的车辆，不发起审批
        OaVehicleApplyDO apply = vehicleApplyMapper.selectById(id);
        assertEquals(reqVO.getVehicleId(), apply.getVehicleId());
        assertEquals(-1, apply.getStatus());
        assertNull(apply.getProcessInstanceId());
        verifyNoInteractions(processInstanceApi);
    }

    @ParameterizedTest
    @EnumSource(OaVehicleStatusEnum.class)
    public void testUpdateVehicleApply_vehicleStatus(OaVehicleStatusEnum status) {
        // mock 数据
        OaVehicleApplyDO apply = randomVehicleApplyDO();
        vehicleApplyMapper.insert(apply);
        // 准备参数
        OaVehicleApplySaveReqVO reqVO = toBean(apply, OaVehicleApplySaveReqVO.class).setReason("调整用车事由");
        // mock 方法
        when(vehicleService.validateVehicleExists(30L)).thenReturn(randomVehicleDO().setStatus(status.getStatus()));

        // 调用
        vehicleApplyService.updateVehicleApply(reqVO, 10L);

        // 断言：车辆状态变化不影响编辑草稿
        OaVehicleApplyDO actual = vehicleApplyMapper.selectById(apply.getId());
        assertEquals(reqVO.getReason(), actual.getReason());
        assertEquals(-1, actual.getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    @ParameterizedTest
    @EnumSource(value = OaVehicleStatusEnum.class, names = {"DISABLED", "IN_USE"})
    public void testSubmitVehicleApply_notIdleVehicle(OaVehicleStatusEnum status) {
        // mock 数据
        OaVehicleApplyDO apply = randomVehicleApplyDO();
        vehicleApplyMapper.insert(apply);
        // mock 方法
        when(vehicleService.validateVehicleExists(30L)).thenReturn(randomVehicleDO().setStatus(status.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> vehicleApplyService.submitVehicleApply(apply.getId(), 10L), VEHICLE_NOT_ENABLED);
        verifyNoInteractions(processInstanceApi);
        OaVehicleApplyDO actual = vehicleApplyMapper.selectById(apply.getId());
        assertEquals(-1, actual.getStatus());
        assertNull(actual.getProcessInstanceId());
    }

    @Test
    public void testSubmitVehicleApply_pastTime() {
        // mock 数据
        OaVehicleApplyDO apply = randomVehicleApplyDO().setStartTime(LocalDateTime.now().minusHours(1));
        vehicleApplyMapper.insert(apply);
        // mock 方法
        when(vehicleService.validateVehicleExists(30L)).thenReturn(randomVehicleDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("past-time");

        // 调用，并断言：允许补录已开始的用车申请
        assertEquals("past-time", vehicleApplyService.submitVehicleApply(apply.getId(), 10L));
        assertEquals(1, vehicleApplyMapper.selectById(apply.getId()).getStatus());
    }

    @Test
    public void testUpdateVehicleApplyStatus_approveAndIdempotent() {
        // mock 数据
        OaVehicleApplyDO apply = randomVehicleApplyDO().setStatus(1).setProcessInstanceId("approved");
        vehicleApplyMapper.insert(apply);

        // 调用
        vehicleApplyService.updateVehicleApplyStatus(apply.getId(), 2);
        vehicleApplyService.updateVehicleApplyStatus(apply.getId(), 2);

        // 断言
        assertEquals(2, vehicleApplyMapper.selectById(apply.getId()).getStatus());
        assertEquals(1, vehicleApplyMapper.selectById(apply.getId()).getReturnStatus());
        assertEquals("approved", vehicleApplyMapper.selectById(apply.getId()).getProcessInstanceId());
    }

    @Test
    public void testUpdateVehicleApplyStatus_returnedNotReset() {
        // mock 数据
        OaVehicleApplyDO apply = randomVehicleApplyDO().setStatus(2).setReturnStatus(3);
        vehicleApplyMapper.insert(apply);

        // 调用
        vehicleApplyService.updateVehicleApplyStatus(apply.getId(), 2);
        vehicleApplyService.updateVehicleApplyStatus(apply.getId(), 3);

        // 断言
        OaVehicleApplyDO actual = vehicleApplyMapper.selectById(apply.getId());
        assertEquals(2, actual.getStatus());
        assertEquals(3, actual.getReturnStatus());
    }

    @Test
    public void testSubmitVehicleApply_synchronousApproval() {
        // mock 数据
        OaVehicleApplyDO apply = randomVehicleApplyDO();
        vehicleApplyMapper.insert(apply);
        // mock 方法
        when(vehicleService.validateVehicleExists(30L)).thenReturn(randomVehicleDO());
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenAnswer(invocation -> {
            eventPublisher.publishEvent(new BpmProcessInstanceStatusEvent(this)
                    .setId("auto-approved").setBusinessKey(apply.getId().toString())
                    .setProcessDefinitionKey(BpmModelConstants.VEHICLE_APPLY).setStatus(2));
            return "auto-approved";
        });

        // 调用
        vehicleApplyService.submitVehicleApply(apply.getId(), 10L);

        // 断言
        assertEquals(2, vehicleApplyMapper.selectById(apply.getId()).getStatus());
        assertEquals("auto-approved", vehicleApplyMapper.selectById(apply.getId()).getProcessInstanceId());
    }

    @Test
    public void testGetVehicleApply_ownerAndPageScope() {
        // mock 数据
        OaVehicleApplyDO own = randomVehicleApplyDO();
        vehicleApplyMapper.insert(own);
        OaVehicleApplyDO other = randomVehicleApplyDO().setUserId(11L);
        vehicleApplyMapper.insert(other);

        // 调用，并断言
        assertEquals(1, vehicleApplyService.getVehicleApplyPage(10L, new OaVehicleApplyPageReqVO()).getTotal());
        assertEquals(other.getId(), vehicleApplyService.getVehicleApply(other.getId()).getId());
        assertServiceException(() -> vehicleApplyService.deleteVehicleApply(other.getId(), 10L), VEHICLE_APPLY_NOT_OWNER);
    }

    @Test
    public void testGetVehicleApplyPage_filters() {
        // mock 数据
        LocalDateTime createTime = LocalDateTime.of(2026, 9, 12, 12, 0);
        OaVehicleApplyDO apply = randomVehicleApplyDO().setNo("YC202609120001")
                .setVehicleNo("沪A12345").setDeptId(40L).setStatus(2).setReturnStatus(1);
        apply.setCreateTime(createTime);
        vehicleApplyMapper.insert(apply);
        // 准备参数
        OaVehicleApplyPageReqVO reqVO = new OaVehicleApplyPageReqVO().setNo("20260912")
                .setVehicleNo("12345").setDeptId(40L).setStatus(2).setReturnStatus(1)
                .setCreateTime(new LocalDateTime[]{createTime, createTime});

        // 调用，并断言：筛选不扩大本人范围，部门和创建时间独立生效
        assertEquals(1, vehicleApplyService.getVehicleApplyPage(10L, reqVO).getTotal());
        assertEquals(0, vehicleApplyService.getVehicleApplyPage(11L, reqVO).getTotal());
        reqVO.setDeptId(41L);
        assertEquals(0, vehicleApplyService.getVehicleApplyPage(10L, reqVO).getTotal());
        reqVO.setDeptId(40L).setCreateTime(new LocalDateTime[]{createTime.plusSeconds(1), createTime.plusDays(1)});
        assertEquals(0, vehicleApplyService.getVehicleApplyPage(10L, reqVO).getTotal());
        reqVO.setCreateTime(null).setVehicleNo("99999");
        assertEquals(0, vehicleApplyService.getVehicleApplyPage(10L, reqVO).getTotal());
    }

    @Test
    public void testUpdateVehicleApply_nullPassengerAndStatusGuard() {
        // mock 数据
        OaVehicleApplyDO apply = randomVehicleApplyDO().setPassenger("外部客户张三");
        vehicleApplyMapper.insert(apply);
        // 准备参数
        OaVehicleApplySaveReqVO reqVO = toBean(apply, OaVehicleApplySaveReqVO.class).setPassenger(null);
        // mock 方法
        when(vehicleService.validateVehicleExists(30L)).thenReturn(randomVehicleDO());

        // 调用
        vehicleApplyService.updateVehicleApply(reqVO, 10L);

        // 断言
        assertEquals("外部客户张三", vehicleApplyMapper.selectById(apply.getId()).getPassenger());
        vehicleApplyMapper.updateById(new OaVehicleApplyDO().setId(apply.getId()).setStatus(1));
        assertServiceException(() -> vehicleApplyService.updateVehicleApply(reqVO, 10L), VEHICLE_APPLY_STATUS_INVALID);
        assertServiceException(() -> vehicleApplyService.deleteVehicleApply(apply.getId(), 10L), VEHICLE_APPLY_STATUS_INVALID);
    }

    @Test
    public void testCancelVehicleApply() {
        // mock 数据
        OaVehicleApplyDO apply = randomVehicleApplyDO().setStatus(1).setProcessInstanceId("cancel");
        vehicleApplyMapper.insert(apply);
        // mock 方法
        doAnswer(invocation -> {
            eventPublisher.publishEvent(new BpmProcessInstanceStatusEvent(this)
                    .setId("cancel").setBusinessKey(apply.getId().toString())
                    .setProcessDefinitionKey(BpmModelConstants.VEHICLE_APPLY).setStatus(4));
            return null;
        }).when(processInstanceApi).cancelProcessInstanceByStartUser(10L, "cancel", "申请人取消用车申请");

        // 调用
        vehicleApplyService.cancelVehicleApply(apply.getId(), 10L);

        // 断言
        assertEquals(4, vehicleApplyMapper.selectById(apply.getId()).getStatus());
        assertEquals(0, vehicleApplyMapper.selectById(apply.getId()).getReturnStatus());
    }

    @Test
    public void testUpdateVehicleApplyReturnStatus_duplicate() {
        // mock 数据
        OaVehicleApplyDO apply = randomVehicleApplyDO().setStatus(2).setReturnStatus(1);
        vehicleApplyMapper.insert(apply);

        // 调用
        vehicleApplyService.updateVehicleApplyReturnStatus(apply.getId(), 1, 2);

        // 断言：已进入还车中的申请不能再次占用
        assertEquals(2, vehicleApplyMapper.selectById(apply.getId()).getReturnStatus());
        assertServiceException(() -> vehicleApplyService.updateVehicleApplyReturnStatus(apply.getId(), 1, 2),
                VEHICLE_RETURN_STATUS_INVALID);
        // 模拟另一请求持有旧状态时，数据库条件更新同样不会覆盖当前状态
        assertEquals(0, vehicleApplyMapper.updateReturnStatusByIdAndReturnStatus(apply.getId(), 1, 2));
    }

    // ========== 随机对象 ==========

    /**
     * 构造具有合法用车时段和固定业务关联的用车申请。
     *
     * @return 未入库的测试对象
     */
    private static OaVehicleApplyDO randomVehicleApplyDO() {
        return randomPojo(OaVehicleApplyDO.class, apply -> apply.setId(null)
                .setVehicleId(30L).setVehicleNo("沪A12345").setUserId(10L).setDeptId(20L)
                .setStartTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0))
                .setEndTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0))
                .setPassenger(null)
                .setStatus(-1).setReturnStatus(0).setProcessInstanceId(null));
    }

    /**
     * 构造可用车辆。
     *
     * @return 未入库的测试对象
     */
    private static OaVehicleDO randomVehicleDO() {
        return randomPojo(OaVehicleDO.class, vehicle -> vehicle.setId(30L).setNo("沪A12345")
                .setStatus(0));
    }

}
