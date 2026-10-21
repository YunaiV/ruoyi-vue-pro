package cn.iocoder.yudao.module.iot.service.device.message;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.enums.DateIntervalEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsDeviceMessageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsDeviceMessageSummaryByDateRespVO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import cn.iocoder.yudao.module.iot.dal.storage.IotDeviceMessageDAO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaTaskRecordService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

/**
 * {@link IotDeviceMessageServiceImpl} 的单元测试
 *
 * 注：时序库没有 embedded 替代，存储 DAO 与依赖 service 走 mock；
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
    private IotDeviceMessageDAO deviceMessageDAO;
    @Mock
    private IotDeviceMessageProducer deviceMessageProducer;

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
        verify(deviceMessageDAO).insert(captor.capture());
        Long actualTs = captor.getValue().getTs();
        assertNotNull(actualTs, "ts 不应为空");
        assertTrue(actualTs >= before && actualTs <= after,
                "ts 应在调用前后区间内； 实际 = " + actualTs);
    }

    @Test
    public void testCreateDeviceLogAsync_idFallback_whenBlank() {
        // 准备：构造一条 id 为空的消息，复合主键字段不能为 null
        IotDeviceMessage message = buildMessage(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod())
                .setId(null);

        // 调用
        service.createDeviceLogAsync(message);

        // 断言：mapper.insert 接收到的 messageDO 已补充 id
        ArgumentCaptor<IotDeviceMessageDO> captor = ArgumentCaptor.forClass(IotDeviceMessageDO.class);
        verify(deviceMessageDAO).insert(captor.capture());
        assertNotNull(captor.getValue().getId(), "id 不应为空");
    }

    @Test
    public void testCreateDeviceLogAsync_swallowMapperException() {
        // 准备：mapper.insert 抛异常，验证 @Async 方法内部 try/catch 兜底，不向上抛
        IotDeviceMessage message = buildMessage(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
        doThrow(new RuntimeException("DB unavailable")).when(deviceMessageDAO).insert(any());

        // 调用 & 断言
        assertDoesNotThrow(() -> service.createDeviceLogAsync(message));
        verify(deviceMessageDAO).insert(any(IotDeviceMessageDO.class));
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
        when(deviceMessageDAO.selectPage(same(reqVO)))
                .thenReturn(new PageResult<>(Collections.singletonList(record), 1L));

        // 调用
        PageResult<IotDeviceMessageDO> result = service.getDeviceMessagePage(reqVO);

        // 断言：表不存在等存储差异，由 DAO 处理（见各 DAO 的单元测试）
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("msg-1", result.getList().get(0).getId());
    }

    // ========== getDeviceMessageListByRequestIdsAndReply ==========

    @Test
    public void testGetDeviceMessageListByRequestIdsAndReply_emptyIds_returnEmpty() {
        // 调用 & 断言：requestIds 为空直接返回空列表，不查 DB
        List<IotDeviceMessageDO> result = service.getDeviceMessageListByRequestIdsAndReply(
                1L, Collections.emptyList(), true);

        assertTrue(result.isEmpty());
        verify(deviceMessageDAO, never()).selectListByRequestIdsAndReply(any(), any(), any());
    }

    @Test
    public void testGetDeviceMessageListByRequestIdsAndReply_normal_delegateToMapper() {
        // 准备
        List<String> requestIds = Collections.singletonList("req-1");
        IotDeviceMessageDO record = new IotDeviceMessageDO().setId("msg-1");
        when(deviceMessageDAO.selectListByRequestIdsAndReply(1L, requestIds, true))
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
        when(deviceMessageDAO.selectCountByCreateTime(isNull())).thenReturn(123L);

        // 调用
        Long count = service.getDeviceMessageCount(null);

        // 断言
        assertEquals(123L, count);
        verify(deviceMessageDAO).selectCountByCreateTime(isNull());
    }

    @Test
    public void testGetDeviceMessageCount_withCreateTime_passEpochMilli() {
        // 准备：service 内部用 LocalDateTimeUtil.toEpochMilli 做转换，断言时也用同函数得到期望值
        LocalDateTime createTime = LocalDateTime.of(2026, 1, 1, 0, 0);
        long expectedMs = LocalDateTimeUtil.toEpochMilli(createTime);
        when(deviceMessageDAO.selectCountByCreateTime(expectedMs)).thenReturn(456L);

        // 调用
        Long count = service.getDeviceMessageCount(createTime);

        // 断言
        assertEquals(456L, count);
    }

    // ========== getDeviceMessageSummaryByDate ==========

    @Test
    public void testGetDeviceMessageSummaryByDate_success() {
        // 准备参数：按天合并 2026-01-01 的小时统计
        IotStatisticsDeviceMessageReqVO reqVO = new IotStatisticsDeviceMessageReqVO();
        reqVO.setInterval(DateIntervalEnum.DAY.getInterval());
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 1, 23, 59, 59);
        reqVO.setTimes(new LocalDateTime[]{startTime, endTime});

        // mock 方法：DAO 统一返回 Timestamp 时间、Long 数量
        when(deviceMessageDAO.selectDeviceMessageCountGroupByDate(
                LocalDateTimeUtil.toEpochMilli(startTime), LocalDateTimeUtil.toEpochMilli(endTime)))
                .thenReturn(Arrays.asList(
                        IotDeviceMessageDAO.buildMessageCount(Timestamp.valueOf(startTime.withHour(1)), 3L, 1L),
                        IotDeviceMessageDAO.buildMessageCount(Timestamp.valueOf(startTime.withHour(2)), 2L, 0L)));

        // 调用
        List<IotStatisticsDeviceMessageSummaryByDateRespVO> list = service.getDeviceMessageSummaryByDate(reqVO);

        // 断言
        assertEquals(1, list.size());
        assertEquals(5, list.get(0).getUpstreamCount());
        assertEquals(1, list.get(0).getDownstreamCount());
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
