package cn.iocoder.yudao.module.pay.service.transfer;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateRespDTO;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import cn.iocoder.yudao.module.pay.dal.mysql.transfer.PayTransferMapper;
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.enums.PayChannelEnum;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum;
import cn.iocoder.yudao.module.pay.framework.pay.config.PayProperties;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.transfer.PayTransferRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.transfer.PayTransferUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link PayTransferServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({PayTransferServiceImpl.class, PayNoRedisDAO.class})
public class PayTransferServiceTest extends BaseDbAndRedisUnitTest {

    @Resource
    private PayTransferServiceImpl transferService;

    @Resource
    private PayTransferMapper transferMapper;

    @MockitoBean
    private PayProperties payProperties;
    @MockitoBean
    private PayAppService appService;
    @MockitoBean
    private PayChannelService channelService;
    @MockitoBean
    private PayNotifyService notifyService;

    @Test
    public void testNotifyTransferProgressing_fillChannelPackageInfoIfAbsent() {
        // mock 数据（PayTransferDO）：同步任务先把 WAITING 更新为 PROCESSING，但是不返回 package 信息
        PayTransferDO transfer = randomPojo(PayTransferDO.class,
                o -> o.setAppId(10L)
                        .setNo("T110")
                        .setStatus(PayTransferStatusEnum.PROCESSING.getStatus())
                        .setChannelPackageInfo(null));
        transferMapper.insert(transfer);
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setAppId(10L));
        PayTransferRespDTO createNotify = PayTransferRespDTO.processingOf("WX_TRANSFER_110",
                "T110", "create");
        createNotify.setChannelPackageInfo("package-info-110");

        // 调用：模拟发起转账接口随后返回 PROCESSING，并携带微信确认收款 package 信息
        transferService.notifyTransfer(channel, createNotify);

        // 断言：已是 PROCESSING 时仍会补写 package 信息
        PayTransferDO dbTransfer = transferMapper.selectById(transfer.getId());
        assertEquals(PayTransferStatusEnum.PROCESSING.getStatus(), dbTransfer.getStatus());
        assertEquals("package-info-110", dbTransfer.getChannelPackageInfo());
    }

    @Test
    public void testNotifyTransferProgressing_notOverwriteChannelPackageInfo() {
        // mock 数据（PayTransferDO）：已经存在 package 信息
        PayTransferDO transfer = randomPojo(PayTransferDO.class,
                o -> o.setAppId(10L)
                        .setNo("T110")
                        .setStatus(PayTransferStatusEnum.PROCESSING.getStatus())
                        .setChannelPackageInfo("package-info-110"));
        transferMapper.insert(transfer);
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setAppId(10L));
        PayTransferRespDTO syncNotify = PayTransferRespDTO.processingOf("WX_TRANSFER_110",
                "T110", "sync");

        // 调用：后续同步任务不返回 package 信息
        transferService.notifyTransfer(channel, syncNotify);

        // 断言：已有 package 信息不被空值覆盖
        PayTransferDO dbTransfer = transferMapper.selectById(transfer.getId());
        assertEquals("package-info-110", dbTransfer.getChannelPackageInfo());
    }

    @Test // 调用 unifiedTransfer 接口，发生异常（例如说：网络超时）
    public void testCreateTransfer_unifiedTransferException() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenThrow(new RuntimeException("模拟网络超时"));
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用：未知结果不抛出异常，保留 WAITING 等待轮询
        PayTransferCreateRespDTO respDTO = transferService.createTransfer(reqDTO);
        // 断言
        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertNotNull(dbTransfer);
        assertEquals(PayTransferStatusEnum.WAITING.getStatus(), dbTransfer.getStatus());
        assertNull(respDTO.getChannelPackageInfo());
        verify(notifyService, never()).createPayNotifyTask(any(), any());
    }

    @Test // 调用 unifiedTransfer 接口，返回 null
    public void testCreateTransfer_nullResponse() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenReturn(null);
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用
        PayTransferCreateRespDTO respDTO = transferService.createTransfer(reqDTO);
        // 断言
        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(PayTransferStatusEnum.WAITING.getStatus(), dbTransfer.getStatus());
        assertNull(respDTO.getChannelPackageInfo());
    }

    @Test // 调用 unifiedTransfer 接口，返回存在渠道错误
    public void testCreateTransfer_channelError() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenAnswer(invocation -> {
            PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
            return PayTransferRespDTO.closedOf("PAYEE_NOT_EXIST", "收款账号不存在",
                    unifiedReqDTO.getOutTransferNo(), "closed");
        });
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用，并断言异常
        assertServiceException(() -> transferService.createTransfer(reqDTO),
                PAY_TRANSFER_SUBMIT_CHANNEL_ERROR, "PAYEE_NOT_EXIST", "收款账号不存在");
        // 断言：关闭状态已落库
        PayTransferDO dbTransfer = transferMapper.selectByAppIdAndMerchantOrderId(1L, reqDTO.getMerchantTransferId());
        assertNotNull(dbTransfer);
        assertEquals(PayTransferStatusEnum.CLOSED.getStatus(), dbTransfer.getStatus());
        assertEquals("PAYEE_NOT_EXIST", dbTransfer.getChannelErrorCode());
        assertEquals("收款账号不存在", dbTransfer.getChannelErrorMsg());
        verify(notifyService).createPayNotifyTask(eq(PayNotifyTypeEnum.TRANSFER.getType()), eq(dbTransfer.getId()));
    }

    @Test // 调用 unifiedTransfer 接口，返回关闭，但不存在渠道错误
    public void testCreateTransfer_closedWithoutChannelError() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenAnswer(invocation -> {
            PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
            return PayTransferRespDTO.closedOf(null, null, unifiedReqDTO.getOutTransferNo(), "closed");
        });
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用
        PayTransferCreateRespDTO respDTO = transferService.createTransfer(reqDTO);
        // 断言
        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(PayTransferStatusEnum.CLOSED.getStatus(), dbTransfer.getStatus());
    }

    @Test // 调用 unifiedTransfer 接口，返回成功
    public void testCreateTransfer_success() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenAnswer(invocation -> {
            PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
            return PayTransferRespDTO.successOf("CHANNEL_T001", LocalDateTime.now(),
                    unifiedReqDTO.getOutTransferNo(), "success");
        });
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用
        PayTransferCreateRespDTO respDTO = transferService.createTransfer(reqDTO);
        // 断言
        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(PayTransferStatusEnum.SUCCESS.getStatus(), dbTransfer.getStatus());
        assertEquals("CHANNEL_T001", dbTransfer.getChannelTransferNo());
        verify(notifyService).createPayNotifyTask(eq(PayNotifyTypeEnum.TRANSFER.getType()), eq(dbTransfer.getId()));
    }

    @Test // 调用 unifiedTransfer 接口，返回转账中
    public void testCreateTransfer_processing() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenAnswer(invocation -> {
            PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
            PayTransferRespDTO resp = PayTransferRespDTO.processingOf("CHANNEL_T001",
                    unifiedReqDTO.getOutTransferNo(), "processing");
            resp.setChannelPackageInfo("package-info-001");
            return resp;
        });
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用
        PayTransferCreateRespDTO respDTO = transferService.createTransfer(reqDTO);
        // 断言
        assertEquals("package-info-001", respDTO.getChannelPackageInfo());
        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(PayTransferStatusEnum.PROCESSING.getStatus(), dbTransfer.getStatus());
        assertEquals("package-info-001", dbTransfer.getChannelPackageInfo());
    }

    @Test // 调用 unifiedTransfer 接口返回成功，但通知转账结果时找不到转账单
    public void testCreateTransfer_notifyTransferNotFound() {
        // mock 方法（client）：返回不存在的转账单号，使 notifyTransfer 抛出异常
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenReturn(PayTransferRespDTO.successOf("CHANNEL_T001",
                LocalDateTime.now(), "NOT_EXISTS", "success"));
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用，并断言异常：非并发更新的通知异常，不能被吞掉
        assertServiceException(() -> transferService.createTransfer(reqDTO), PAY_TRANSFER_NOT_FOUND);
        // 断言
        PayTransferDO dbTransfer = transferMapper.selectByAppIdAndMerchantOrderId(1L, reqDTO.getMerchantTransferId());
        assertEquals(PayTransferStatusEnum.WAITING.getStatus(), dbTransfer.getStatus());
    }

    @Test // 调用 unifiedTransfer 接口返回存在渠道错误，但创建转账通知任务时发生异常
    public void testCreateTransfer_channelErrorAndNotifyTaskException() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenAnswer(invocation -> {
            PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
            return PayTransferRespDTO.closedOf("PAYEE_NOT_EXIST", "收款账号不存在",
                    unifiedReqDTO.getOutTransferNo(), "closed");
        });
        // mock 方法（notify）
        RuntimeException notifyTaskException = new RuntimeException("模拟创建通知任务异常");
        doThrow(notifyTaskException).when(notifyService).createPayNotifyTask(any(), any());
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用，并断言异常：抛出真实的通知异常，而不是渠道错误
        RuntimeException ex = assertThrows(RuntimeException.class, () -> transferService.createTransfer(reqDTO));
        assertSame(notifyTaskException, ex);
        // 断言：关闭状态随通知事务回滚
        PayTransferDO dbTransfer = transferMapper.selectByAppIdAndMerchantOrderId(1L, reqDTO.getMerchantTransferId());
        assertEquals(PayTransferStatusEnum.WAITING.getStatus(), dbTransfer.getStatus());
        assertNull(dbTransfer.getChannelErrorCode());
    }

    @Test // 调用 unifiedTransfer 接口返回存在渠道错误，但转账单已被并发更新为成功
    public void testCreateTransfer_channelErrorWhenConcurrentSuccess() {
        // mock 方法（client）：返回前，转账回调已经将转账单更新为成功
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenAnswer(invocation -> {
            PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
            updateTransferStatus(unifiedReqDTO.getOutTransferNo(), PayTransferStatusEnum.SUCCESS.getStatus());
            return PayTransferRespDTO.closedOf("PAYEE_NOT_EXIST", "收款账号不存在",
                    unifiedReqDTO.getOutTransferNo(), "closed");
        });
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用：以最新的成功状态为准，不误报渠道错误
        PayTransferCreateRespDTO respDTO = transferService.createTransfer(reqDTO);
        // 断言
        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(PayTransferStatusEnum.SUCCESS.getStatus(), dbTransfer.getStatus());
        assertNull(dbTransfer.getChannelErrorCode());
        verify(notifyService, never()).createPayNotifyTask(any(), any());
    }

    @Test // 调用 unifiedTransfer 接口返回成功，但转账单已被并发更新为关闭
    public void testCreateTransfer_successWhenConcurrentClosed() {
        // mock 方法（client）：返回前，转账轮询已经将转账单更新为关闭
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenAnswer(invocation -> {
            PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
            updateTransferStatus(unifiedReqDTO.getOutTransferNo(), PayTransferStatusEnum.CLOSED.getStatus());
            return PayTransferRespDTO.successOf("CHANNEL_T001", LocalDateTime.now(),
                    unifiedReqDTO.getOutTransferNo(), "success");
        });
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用：以最新的关闭状态为准
        PayTransferCreateRespDTO respDTO = transferService.createTransfer(reqDTO);
        // 断言
        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(PayTransferStatusEnum.CLOSED.getStatus(), dbTransfer.getStatus());
        verify(notifyService, never()).createPayNotifyTask(any(), any());
    }

    @Test // 调用 unifiedTransfer 接口返回存在渠道错误，通知时发生状态竞争，且转账单已被并发更新为关闭
    public void testCreateTransfer_channelErrorWhenConcurrentClosed() {
        PayTransferServiceImpl transferServiceImpl = mock(PayTransferServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayTransferServiceImpl.class)))
                    .thenReturn(transferServiceImpl);
            doThrow(exception(PAY_TRANSFER_NOTIFY_FAIL_STATUS_NOT_WAITING_OR_PROCESSING))
                    .when(transferServiceImpl).notifyTransfer(any(PayChannelDO.class), any());
            // mock 方法（client）：返回前，转账轮询已经将转账单更新为关闭
            PayClient<?> client = mockPayClient();
            when(client.unifiedTransfer(any())).thenAnswer(invocation -> {
                PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
                updateTransferStatus(unifiedReqDTO.getOutTransferNo(), PayTransferStatusEnum.CLOSED.getStatus());
                return PayTransferRespDTO.closedOf("PAYEE_NOT_EXIST", "收款账号不存在",
                        unifiedReqDTO.getOutTransferNo(), "closed");
            });
            // 准备参数
            PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

            // 调用，并断言异常：已确认关闭，提示渠道错误
            assertServiceException(() -> transferService.createTransfer(reqDTO),
                    PAY_TRANSFER_SUBMIT_CHANNEL_ERROR, "PAYEE_NOT_EXIST", "收款账号不存在");
        }
    }

    @Test // 调用 unifiedTransfer 接口返回存在渠道错误，通知时发生状态竞争，但转账单仍是待转账
    public void testCreateTransfer_channelErrorWhenConcurrentWaiting() {
        PayTransferServiceImpl transferServiceImpl = mock(PayTransferServiceImpl.class);
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PayTransferServiceImpl.class)))
                    .thenReturn(transferServiceImpl);
            doThrow(exception(PAY_TRANSFER_NOTIFY_FAIL_STATUS_NOT_WAITING_OR_PROCESSING))
                    .when(transferServiceImpl).notifyTransfer(any(PayChannelDO.class), any());
            // mock 方法（client）
            PayClient<?> client = mockPayClient();
            when(client.unifiedTransfer(any())).thenAnswer(invocation -> {
                PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
                return PayTransferRespDTO.closedOf("PAYEE_NOT_EXIST", "收款账号不存在",
                        unifiedReqDTO.getOutTransferNo(), "closed");
            });
            // 准备参数
            PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

            // 调用，并断言异常：无法确认最新状态，抛出原通知异常
            assertServiceException(() -> transferService.createTransfer(reqDTO),
                    PAY_TRANSFER_NOTIFY_FAIL_STATUS_NOT_WAITING_OR_PROCESSING);
            // 断言
            PayTransferDO dbTransfer = transferMapper.selectByAppIdAndMerchantOrderId(1L, reqDTO.getMerchantTransferId());
            assertEquals(PayTransferStatusEnum.WAITING.getStatus(), dbTransfer.getStatus());
        }
    }

    private PayClient<?> mockPayClient() {
        // mock 方法（app）
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L).setTransferNotifyUrl("http://127.0.0.1/transfer"));
        when(appService.validPayApp(eq("demo"))).thenReturn(app);
        // mock 方法（channel）
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L).setAppId(1L)
                .setCode(PayChannelEnum.ALIPAY_PC.getCode()));
        when(channelService.validPayChannel(eq(1L), eq(PayChannelEnum.ALIPAY_PC.getCode()))).thenReturn(channel);
        when(payProperties.getTransferNotifyUrl()).thenReturn("http://127.0.0.1");
        // mock 方法（client）
        PayClient<?> client = mock(PayClient.class);
        when(channelService.getPayClient(eq(10L))).thenReturn(client);
        return client;
    }

    private void updateTransferStatus(String no, Integer status) {
        PayTransferDO transfer = transferMapper.selectByAppIdAndNo(1L, no);
        transferMapper.updateById(new PayTransferDO().setId(transfer.getId()).setStatus(status));
    }

    private static PayTransferCreateReqDTO buildTransferCreateReqDTO() {
        return new PayTransferCreateReqDTO().setAppKey("demo").setChannelCode(PayChannelEnum.ALIPAY_PC.getCode())
                .setUserIp("127.0.0.1").setUserId(1L).setUserType(1)
                .setMerchantTransferId("1001").setSubject("佣金提现").setPrice(100)
                .setUserAccount("test@example.com").setUserName("芋艿");
    }

}
