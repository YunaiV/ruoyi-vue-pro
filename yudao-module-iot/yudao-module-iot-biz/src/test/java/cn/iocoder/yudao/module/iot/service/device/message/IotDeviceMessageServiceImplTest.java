package cn.iocoder.yudao.module.iot.service.device.message;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDeviceMessageMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaTaskRecordService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * {@link IotDeviceMessageServiceImpl} 的单元测试
 *
 * 注：TDengine 数据源没有 embedded 替代，mapper 与依赖 service 走 mock；
 * handleUpstreamDeviceMessage 与 sendDeviceMessage 下行成功路径依赖 SpringUtil.getBean 的自调用
 * createDeviceLogAsync，更适合放到集成测试，本类不展开。
 *
 * @author 芋道源码
 */
public class IotDeviceMessageServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotDeviceMessageServiceImpl service;

    @Mock
    private IotDeviceService deviceService;
    @Mock
    private IotDevicePropertyService devicePropertyService;
    @Mock
    private IotOtaTaskRecordService otaTaskRecordService;
    @Mock
    private IotDeviceMessageMapper deviceMessageMapper;
    @Mock
    private IotDeviceMessageProducer deviceMessageProducer;

    // ========== defineDeviceMessageStable ==========

    @Test
    public void testDefineDeviceMessageStable_whenTableExists_skipCreate() {
        // 准备：showSTable 返回非空 → 表已存在
        when(deviceMessageMapper.showSTable()).thenReturn("device_message");

        // 调用
        service.defineDeviceMessageStable();

        // 断言：跳过 createSTable
        verify(deviceMessageMapper, never()).createSTable();
    }

    @Test
    public void testDefineDeviceMessageStable_whenTableMissing_create() {
        // 准备：showSTable 返回空 → 表不存在
        when(deviceMessageMapper.showSTable()).thenReturn("");

        // 调用
        service.defineDeviceMessageStable();

        // 断言：触发 createSTable
        verify(deviceMessageMapper, times(1)).createSTable();
    }

    // ========== createDeviceLogAsync ==========

    @Test
    public void testCreateDeviceLogAsync_tsFallback_whenNull() {
        // 准备：构造一条 ts 为 null 的消息
        IotDeviceMessage message = buildMessage(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
        long before = System.currentTimeMillis();

        // 调用
        service.createDeviceLogAsync(message);
        long after = System.currentTimeMillis();

        // 断言：mapper.insert 接收到的 messageDO 已被填上 ts，值在 [before, after] 区间
        ArgumentCaptor<IotDeviceMessageDO> captor = ArgumentCaptor.forClass(IotDeviceMessageDO.class);
        verify(deviceMessageMapper).insert(captor.capture());
        Long actualTs = captor.getValue().getTs();
        assertNotNull(actualTs, "ts 不应为空");
        assertTrue(actualTs >= before && actualTs <= after,
                "ts 应在调用前后区间内； 实际 = " + actualTs);
    }

    @Test
    public void testCreateDeviceLogAsync_swallowMapperException() {
        // 准备：mapper.insert 抛异常，验证 @Async 方法内部 try/catch 兜底，不向上抛
        IotDeviceMessage message = buildMessage(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
        doThrow(new RuntimeException("DB unavailable")).when(deviceMessageMapper).insert(any());

        // 调用 & 断言
        assertDoesNotThrow(() -> service.createDeviceLogAsync(message));
        verify(deviceMessageMapper).insert(any(IotDeviceMessageDO.class));
    }

    // ========== sendDeviceMessage ==========

    @Test
    public void testSendDeviceMessage_upstream_publishToProducer() {
        // 准备：上行消息（PROPERTY_POST）
        IotDeviceDO device = buildDevice();
        IotDeviceMessage message = buildMessage(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());

        // 调用
        IotDeviceMessage result = service.sendDeviceMessage(message, device);

        // 断言：走 producer.sendDeviceMessage，不进入下行链路
        assertSame(message, result);
        verify(deviceMessageProducer, times(1)).sendDeviceMessage(message);
        verify(deviceMessageProducer, never()).sendDeviceMessageToGateway(any(), any());
        verify(devicePropertyService, never()).getDeviceServerId(any());
    }

    @Test
    public void testSendDeviceMessage_downstream_serverIdMissing_throwException() {
        // 准备：下行消息（SERVICE_INVOKE）；devicePropertyService 也查不到 serverId
        IotDeviceDO device = buildDevice();
        IotDeviceMessage message = buildMessage(IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod());
        when(devicePropertyService.getDeviceServerId(device.getId())).thenReturn(null);

        // 调用 & 断言：抛 DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.sendDeviceMessage(message, device));
        assertEquals(1_050_003_007, ex.getCode().intValue());
        verify(deviceMessageProducer, never()).sendDeviceMessageToGateway(any(), any());
    }

    // ========== getDeviceMessagePage ==========

    @Test
    public void testGetDeviceMessagePage_normal() {
        // 准备
        IotDeviceMessagePageReqVO reqVO = new IotDeviceMessagePageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        IotDeviceMessageDO record = new IotDeviceMessageDO().setId("msg-1");
        Page<IotDeviceMessageDO> page = new Page<>(1, 10, 1L);
        page.setRecords(Collections.singletonList(record));
        when(deviceMessageMapper.selectPage(any(), eq(reqVO))).thenReturn(page);

        // 调用
        PageResult<IotDeviceMessageDO> result = service.getDeviceMessagePage(reqVO);

        // 断言
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("msg-1", result.getList().get(0).getId());
    }

    @Test
    public void testGetDeviceMessagePage_whenTableMissing_returnEmpty() {
        // 准备：mapper 抛 "Table does not exist" → 视为表未创建，返回空结果
        IotDeviceMessagePageReqVO reqVO = new IotDeviceMessagePageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        when(deviceMessageMapper.selectPage(any(), any()))
                .thenThrow(new RuntimeException("Table does not exist"));

        // 调用
        PageResult<IotDeviceMessageDO> result = service.getDeviceMessagePage(reqVO);

        // 断言
        assertEquals(0L, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    public void testGetDeviceMessagePage_otherException_rethrow() {
        // 准备：mapper 抛非「表不存在」的异常 → 应向上抛
        IotDeviceMessagePageReqVO reqVO = new IotDeviceMessagePageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        when(deviceMessageMapper.selectPage(any(), any()))
                .thenThrow(new RuntimeException("Connection refused"));

        // 调用 & 断言
        assertThrows(RuntimeException.class, () -> service.getDeviceMessagePage(reqVO));
    }

    // ========== getDeviceMessageListByRequestIdsAndReply ==========

    @Test
    public void testGetDeviceMessageListByRequestIdsAndReply_emptyIds_returnEmpty() {
        // 调用 & 断言：requestIds 为空直接返回空列表，不查 DB
        List<IotDeviceMessageDO> result = service.getDeviceMessageListByRequestIdsAndReply(
                1L, Collections.emptyList(), true);

        assertTrue(result.isEmpty());
        verify(deviceMessageMapper, never()).selectListByRequestIdsAndReply(any(), any(), any());
    }

    @Test
    public void testGetDeviceMessageListByRequestIdsAndReply_normal_delegateToMapper() {
        // 准备
        List<String> requestIds = Collections.singletonList("req-1");
        IotDeviceMessageDO record = new IotDeviceMessageDO().setId("msg-1");
        when(deviceMessageMapper.selectListByRequestIdsAndReply(1L, requestIds, true))
                .thenReturn(Collections.singletonList(record));

        // 调用
        List<IotDeviceMessageDO> result = service.getDeviceMessageListByRequestIdsAndReply(
                1L, requestIds, true);

        // 断言
        assertEquals(1, result.size());
        assertEquals("msg-1", result.get(0).getId());
    }

    // ========== getDeviceMessageCount ==========

    @Test
    public void testGetDeviceMessageCount_whenCreateTimeNull_passNullToMapper() {
        // 准备
        when(deviceMessageMapper.selectCountByCreateTime(isNull())).thenReturn(123L);

        // 调用
        Long count = service.getDeviceMessageCount(null);

        // 断言
        assertEquals(123L, count);
        verify(deviceMessageMapper).selectCountByCreateTime(isNull());
    }

    @Test
    public void testGetDeviceMessageCount_withCreateTime_passEpochMilli() {
        // 准备：service 内部用 LocalDateTimeUtil.toEpochMilli 做转换，断言时也用同函数得到期望值
        LocalDateTime createTime = LocalDateTime.of(2026, 1, 1, 0, 0);
        long expectedMs = LocalDateTimeUtil.toEpochMilli(createTime);
        when(deviceMessageMapper.selectCountByCreateTime(expectedMs)).thenReturn(456L);

        // 调用
        Long count = service.getDeviceMessageCount(createTime);

        // 断言
        assertEquals(456L, count);
    }

    // ========== 辅助方法 ==========

    /** 构造一条最简消息（指定 method 决定上下行分支） */
    private IotDeviceMessage buildMessage(String method) {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setId("msg-1");
        message.setDeviceId(2L);
        message.setMethod(method);
        message.setParams(new HashMap<>());
        return message;
    }

    /** 构造最简设备 */
    private IotDeviceDO buildDevice() {
        return IotDeviceDO.builder().id(2L).build();
    }

}
