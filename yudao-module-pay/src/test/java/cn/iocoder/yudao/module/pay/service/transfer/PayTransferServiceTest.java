package cn.iocoder.yudao.module.pay.service.transfer;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateRespDTO;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import cn.iocoder.yudao.module.pay.dal.mysql.transfer.PayTransferMapper;
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.dal.redis.transfer.PayTransferLockRedisDAO;
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
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link PayTransferServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({PayTransferServiceImpl.class, PayNoRedisDAO.class, PayTransferLockRedisDAO.class})
public class PayTransferServiceTest extends BaseDbAndRedisUnitTest {

    @Resource
    private PayTransferServiceImpl transferService;

    @Resource
    private PayTransferMapper transferMapper;

    @Resource
    private PlatformTransactionManager transactionManager;

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

    @Test // 调用 unifiedTransfer 接口发生异常，查询返回关闭（例如说：钱包查询不到转账单）
    public void testCreateTransfer_unifiedTransferException_queryClosed() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenThrow(new RuntimeException("模拟网络超时"));
        when(client.getTransfer(any())).thenAnswer(invocation ->
                PayTransferRespDTO.closedOf("ORDER_NOT_EXIST", "转账单不存在", invocation.getArgument(0), "closed"));
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用：一次查询的关闭（可能是渠道转账单尚未生成），不作为最终结果，保留 WAITING 交给转账轮询
        PayTransferCreateRespDTO respDTO = transferService.createTransfer(reqDTO);
        // 断言
        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(PayTransferStatusEnum.WAITING.getStatus(), dbTransfer.getStatus());
        assertNull(dbTransfer.getChannelErrorCode());
        verify(client).getTransfer(any());
        verify(notifyService, never()).createPayNotifyTask(any(), any());
    }

    @Test // 调用 unifiedTransfer 接口发生异常，查询返回 WAITING（例如说：支付宝查询不到转账单）
    public void testCreateTransfer_unifiedTransferException_queryWaiting() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenThrow(new RuntimeException("模拟网络超时"));
        when(client.getTransfer(any())).thenAnswer(invocation ->
                PayTransferRespDTO.waitingOf(null, invocation.getArgument(0), "waiting"));
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用：结果未知，保留 WAITING 交给转账轮询
        PayTransferCreateRespDTO respDTO = transferService.createTransfer(reqDTO);
        // 断言
        assertEquals(PayTransferStatusEnum.WAITING.getStatus(), transferMapper.selectById(respDTO.getId()).getStatus());
        verify(notifyService, never()).createPayNotifyTask(any(), any());
    }

    @Test // 调用 unifiedTransfer 接口返回关闭，但查询到渠道转账中（例如说：上一次实际已受理）
    public void testCreateTransfer_closed_queryProcessing() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenAnswer(invocation -> {
            PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
            return PayTransferRespDTO.closedOf("SYSTEM_ERROR", "系统繁忙", unifiedReqDTO.getOutTransferNo(), "closed");
        });
        when(client.getTransfer(any())).thenAnswer(invocation ->
                PayTransferRespDTO.processingOf("CHANNEL_T001", invocation.getArgument(0), "processing"));
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用：渠道实际已受理转账，以查询结果为准，不向调用方误报渠道错误
        PayTransferCreateRespDTO respDTO = transferService.createTransfer(reqDTO);
        // 断言
        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(PayTransferStatusEnum.PROCESSING.getStatus(), dbTransfer.getStatus());
        assertNull(dbTransfer.getChannelErrorCode());
        verify(notifyService, never()).createPayNotifyTask(any(), any());
    }

    @Test // 调用 unifiedTransfer 接口返回关闭，但查询到已有渠道转账单号的待转账（例如说：上一次实际已受理）
    public void testCreateTransfer_closed_queryWaitingWithChannelTransferNo() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenAnswer(invocation -> {
            PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
            return PayTransferRespDTO.closedOf("SYSTEM_ERROR", "系统繁忙", unifiedReqDTO.getOutTransferNo(), "closed");
        });
        when(client.getTransfer(any())).thenAnswer(invocation ->
                PayTransferRespDTO.waitingOf("CHANNEL_T001", invocation.getArgument(0), "waiting"));
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用：渠道实际已受理转账，以查询结果为准，不向调用方误报渠道错误
        PayTransferCreateRespDTO respDTO = transferService.createTransfer(reqDTO);
        // 断言：仍是 WAITING，并保存渠道转账单号、渠道返回数据
        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(PayTransferStatusEnum.WAITING.getStatus(), dbTransfer.getStatus());
        assertEquals("CHANNEL_T001", dbTransfer.getChannelTransferNo());
        assertNotNull(dbTransfer.getChannelNotifyData());
        assertNull(dbTransfer.getChannelErrorCode());
        verify(notifyService, never()).createPayNotifyTask(any(), any());
    }

    @Test // 待转账的通知晚于转账中到达，不能覆盖转账中的转账单
    public void testNotifyTransferWaiting_whenProcessing() {
        // mock 数据（PayTransferDO）
        PayTransferDO transfer = randomPojo(PayTransferDO.class, o -> o.setAppId(10L).setNo("T110")
                .setStatus(PayTransferStatusEnum.PROCESSING.getStatus()).setChannelTransferNo("WX_TRANSFER_110"));
        transferMapper.insert(transfer);
        // 准备参数
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setAppId(10L));

        // 调用
        transferService.notifyTransfer(channel, PayTransferRespDTO.waitingOf("WX_TRANSFER_111", "T110", "waiting"));
        // 断言：不更新
        PayTransferDO dbTransfer = transferMapper.selectById(transfer.getId());
        assertEquals(PayTransferStatusEnum.PROCESSING.getStatus(), dbTransfer.getStatus());
        assertEquals("WX_TRANSFER_110", dbTransfer.getChannelTransferNo());
        assertEquals(transfer.getChannelNotifyData(), dbTransfer.getChannelNotifyData());
    }

    @Test // 并发使用相同的商户转账编号发起转账，只能创建一个转账单，避免重复转账
    public void testCreateTransfer_concurrentCreate() throws Exception {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenAnswer(invocation -> {
            PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
            return PayTransferRespDTO.processingOf("CHANNEL_T001", unifiedReqDTO.getOutTransferNo(), "processing");
        });
        // mock 方法（mapper）：查询后等待一段时间，放大 select 后 insert 的时间窗口
        PayTransferMapper transferMapperSpy = mock(PayTransferMapper.class, delegatesTo(transferMapper));
        doAnswer(invocation -> {
            PayTransferDO transfer = transferMapper.selectByAppIdAndMerchantOrderId(invocation.getArgument(0),
                    invocation.getArgument(1));
            Thread.sleep(300);
            return transfer;
        }).when(transferMapperSpy).selectByAppIdAndMerchantOrderId(any(), any());
        Object target = AopTestUtils.getTargetObject(transferService);
        ReflectionTestUtils.setField(target, "transferMapper", transferMapperSpy);
        List<Throwable> exceptions = new ArrayList<>();
        try {
            // 调用：两个请求并发发起转账
            List<CompletableFuture<PayTransferCreateRespDTO>> futures = List.of(
                    CompletableFuture.supplyAsync(() -> transferService.createTransfer(buildTransferCreateReqDTO())),
                    CompletableFuture.supplyAsync(() -> transferService.createTransfer(buildTransferCreateReqDTO())));
            for (CompletableFuture<PayTransferCreateRespDTO> future : futures) {
                try {
                    future.get();
                } catch (Exception ex) {
                    exceptions.add(ex.getCause());
                }
            }
        } finally {
            ReflectionTestUtils.setField(target, "transferMapper", transferMapper);
        }
        // 断言：只有一个请求创建转账单，另一个请求提示转账单不是关闭状态
        assertEquals(1, exceptions.size());
        assertEquals(PAY_TRANSFER_CREATE_FAIL_STATUS_NOT_CLOSED.getCode(), ((ServiceException) exceptions.get(0)).getCode());
        assertEquals(1, transferMapper.selectList().size());
        verify(client, times(1)).unifiedTransfer(any());
    }

    @Test // 调用 unifiedTransfer 接口返回关闭，查询返回 WAITING（例如说：支付宝查询不到转账单）
    public void testCreateTransfer_closed_queryWaiting() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenAnswer(invocation -> {
            PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
            return PayTransferRespDTO.closedOf("PAYEE_NOT_EXIST", "收款账号不存在",
                    unifiedReqDTO.getOutTransferNo(), "closed");
        });
        when(client.getTransfer(any())).thenAnswer(invocation ->
                PayTransferRespDTO.waitingOf(null, invocation.getArgument(0), "waiting"));
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用，并断言异常：以渠道明确返回的关闭为准
        assertServiceException(() -> transferService.createTransfer(reqDTO),
                PAY_TRANSFER_SUBMIT_CHANNEL_ERROR, "PAYEE_NOT_EXIST", "收款账号不存在");
        // 断言
        PayTransferDO dbTransfer = transferMapper.selectByAppIdAndMerchantOrderId(1L, reqDTO.getMerchantTransferId());
        assertEquals(PayTransferStatusEnum.CLOSED.getStatus(), dbTransfer.getStatus());
        assertEquals("PAYEE_NOT_EXIST", dbTransfer.getChannelErrorCode());
    }

    @Test // 调用 unifiedTransfer 接口发生异常，但查询确认渠道转账成功
    public void testCreateTransfer_unifiedTransferException_querySuccess() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenThrow(new RuntimeException("模拟网络超时"));
        when(client.getTransfer(any())).thenAnswer(invocation ->
                PayTransferRespDTO.successOf("CHANNEL_T001", LocalDateTime.now(),
                        invocation.getArgument(0), "success"));
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用：查询确认渠道转账成功
        PayTransferCreateRespDTO respDTO = transferService.createTransfer(reqDTO);
        // 断言：按查询结果落库为成功
        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(PayTransferStatusEnum.SUCCESS.getStatus(), dbTransfer.getStatus());
        verify(notifyService).createPayNotifyTask(eq(PayNotifyTypeEnum.TRANSFER.getType()), eq(dbTransfer.getId()));
    }

    @Test // 调用 unifiedTransfer 接口发生异常，查询渠道状态也发生异常
    public void testCreateTransfer_unifiedTransferException_queryException() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenThrow(new RuntimeException("模拟网络超时"));
        when(client.getTransfer(any())).thenThrow(new RuntimeException("模拟查询超时"));
        // 准备参数
        PayTransferCreateReqDTO reqDTO = buildTransferCreateReqDTO();

        // 调用：查询失败时保留 WAITING，交给后续轮询补偿
        PayTransferCreateRespDTO respDTO = transferService.createTransfer(reqDTO);
        // 断言
        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(PayTransferStatusEnum.WAITING.getStatus(), dbTransfer.getStatus());
        verify(client).getTransfer(any());
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
        assertEquals("CHANNEL_T001", dbTransfer.getChannelTransferNo());
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

    @Test // 已存在相同的转账单，且不是转账关闭
    public void testCreateTransfer_transferExists() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        // mock 数据：已存在转账成功的转账单
        PayTransferDO transfer = insertClosedTransfer();
        transferMapper.updateById(new PayTransferDO().setId(transfer.getId())
                .setStatus(PayTransferStatusEnum.SUCCESS.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> transferService.createTransfer(buildTransferCreateReqDTO()),
                PAY_TRANSFER_CREATE_FAIL_STATUS_NOT_CLOSED);
        verify(client, never()).unifiedTransfer(any());
    }

    @Test // 转账关闭后再次发起，但转账渠道不一致
    public void testCreateTransfer_retryAfterClosed_channelNotMatch() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        // mock 数据：已存在其它渠道的关闭的转账单
        PayTransferDO transfer = insertClosedTransfer();
        transferMapper.updateById(new PayTransferDO().setId(transfer.getId())
                .setChannelCode(PayChannelEnum.WX_PUB.getCode()));

        // 调用，并断言异常
        assertServiceException(() -> transferService.createTransfer(buildTransferCreateReqDTO()),
                PAY_TRANSFER_CREATE_CHANNEL_NOT_MATCH);
        assertEquals(PayTransferStatusEnum.CLOSED.getStatus(), transferMapper.selectById(transfer.getId()).getStatus());
        verify(client, never()).unifiedTransfer(any());
    }

    @Test // 转账关闭后再次发起，但转账金额不一致
    public void testCreateTransfer_retryAfterClosed_priceNotMatch() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        // mock 数据：已存在金额为 50 的关闭的转账单
        PayTransferDO transfer = insertClosedTransfer();
        transferMapper.updateById(new PayTransferDO().setId(transfer.getId()).setPrice(50));

        // 调用，并断言异常
        assertServiceException(() -> transferService.createTransfer(buildTransferCreateReqDTO()),
                PAY_TRANSFER_CREATE_PRICE_NOT_MATCH);
        assertEquals(PayTransferStatusEnum.CLOSED.getStatus(), transferMapper.selectById(transfer.getId()).getStatus());
        verify(client, never()).unifiedTransfer(any());
    }

    @Test // 转账关闭后再次发起，渠道结果未知，保留 WAITING 并清空上一次的渠道结果
    public void testCreateTransfer_retryAfterClosed_unknown() {
        // mock 方法（client 调用发生异常，查询也发生异常）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenThrow(new RuntimeException("模拟网络超时"));
        when(client.getTransfer(any())).thenThrow(new RuntimeException("模拟查询超时"));
        // mock 数据：已存在关闭的转账单
        PayTransferDO closedTransfer = insertClosedTransfer();

        // 调用
        PayTransferCreateRespDTO respDTO = transferService.createTransfer(buildTransferCreateReqDTO());
        // 断言：复用原转账单号，状态重置为 WAITING，并清空上一次转账的渠道结果
        assertEquals(closedTransfer.getId(), respDTO.getId());
        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(PayTransferStatusEnum.WAITING.getStatus(), dbTransfer.getStatus());
        assertNull(dbTransfer.getChannelTransferNo());
        assertNull(dbTransfer.getSuccessTime());
        assertNull(dbTransfer.getChannelErrorCode());
        assertNull(dbTransfer.getChannelErrorMsg());
        assertNull(dbTransfer.getChannelNotifyData());
        assertNull(dbTransfer.getChannelPackageInfo());
        assertEquals("佣金提现", dbTransfer.getSubject());
        assertEquals("test@example.com", dbTransfer.getUserAccount());
        assertEquals("芋艿", dbTransfer.getUserName());
        assertEquals(Map.of("sceneId", "1000"), dbTransfer.getChannelExtras());
        assertEquals("http://127.0.0.1/transfer", dbTransfer.getNotifyUrl());
        // 断言：渠道编号更新为本次按渠道编码校验的渠道
        assertEquals(10L, dbTransfer.getChannelId());
        verify(client).unifiedTransfer(argThat(unifiedReqDTO ->
                closedTransfer.getNo().equals(unifiedReqDTO.getOutTransferNo())));
    }

    @Test // 转账关闭后再次发起，本次请求没有传递可选字段，需要清空上一次的值
    public void testCreateTransfer_retryAfterClosed_clearOptionalFields() {
        // mock 方法（client 调用发生异常，查询也发生异常）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenThrow(new RuntimeException("模拟网络超时"));
        when(client.getTransfer(any())).thenThrow(new RuntimeException("模拟查询超时"));
        // mock 方法（app）：没有配置转账回调地址
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L).setTransferNotifyUrl(null));
        when(appService.validPayApp(eq("demo"))).thenReturn(app);
        // mock 数据：已存在关闭的转账单
        PayTransferDO closedTransfer = insertClosedTransfer();

        // 调用：本次请求没有传递收款人姓名、渠道额外参数
        PayTransferCreateRespDTO respDTO = transferService.createTransfer(buildTransferCreateReqDTO()
                .setUserName(null).setChannelExtras(null));
        // 断言：上一次的收款人姓名、渠道额外参数、回调地址已清空
        assertEquals(closedTransfer.getId(), respDTO.getId());
        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(PayTransferStatusEnum.WAITING.getStatus(), dbTransfer.getStatus());
        assertNull(dbTransfer.getUserName());
        assertNull(dbTransfer.getChannelExtras());
        assertNull(dbTransfer.getNotifyUrl());
        assertEquals(10L, dbTransfer.getChannelId());
    }

    @Test // 转账关闭后再次发起，但转账单已被并发重新发起
    public void testCreateTransfer_retryAfterClosed_concurrentRetry() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        // mock 数据：已存在关闭的转账单
        PayTransferDO closedTransfer = insertClosedTransfer();
        // mock 方法（mapper）：校验通过后、更新前，其它请求已将转账单重新发起为 WAITING
        PayTransferMapper transferMapperSpy = mock(PayTransferMapper.class, delegatesTo(transferMapper));
        doAnswer(invocation -> {
            updateTransferStatus(closedTransfer.getNo(), PayTransferStatusEnum.WAITING.getStatus());
            return transferMapper.updateByIdAndStatusAndClearChannelResult(invocation.getArgument(0),
                    invocation.getArgument(1), invocation.getArgument(2));
        }).when(transferMapperSpy).updateByIdAndStatusAndClearChannelResult(any(), any(), any());
        Object target = AopTestUtils.getTargetObject(transferService);
        ReflectionTestUtils.setField(target, "transferMapper", transferMapperSpy);
        try {
            // 调用，并断言异常
            assertServiceException(() -> transferService.createTransfer(buildTransferCreateReqDTO()),
                    PAY_TRANSFER_CREATE_FAIL_STATUS_NOT_CLOSED);
        } finally {
            ReflectionTestUtils.setField(target, "transferMapper", transferMapper);
        }
        // 断言：不会再次向渠道发起转账
        verify(client, never()).unifiedTransfer(any());
        assertEquals(PayTransferStatusEnum.WAITING.getStatus(), transferMapper.selectById(closedTransfer.getId()).getStatus());
    }

    @Test // 调用方存在事务，转账关闭后调用方事务回滚，转账关闭的结果仍需保存，并可使用原转账单号重新发起
    public void testCreateTransfer_outerTransactionRollback() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenAnswer(invocation -> {
            PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
            return PayTransferRespDTO.closedOf("PAYEE_NOT_EXIST", "收款账号不存在",
                    unifiedReqDTO.getOutTransferNo(), "closed");
        });
        PayTransferDO outerTransfer = randomPojo(PayTransferDO.class, o -> o.setAppId(1L).setMerchantTransferId("2002"));

        // 调用：调用方事务中，先写入自己的数据，再发起转账，渠道错误导致调用方事务回滚
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        assertServiceException(() -> transactionTemplate.executeWithoutResult(status -> {
            transferMapper.insert(outerTransfer);
            transferService.createTransfer(buildTransferCreateReqDTO());
        }), PAY_TRANSFER_SUBMIT_CHANNEL_ERROR, "PAYEE_NOT_EXIST", "收款账号不存在");
        // 断言：调用方事务已回滚，但转账关闭的结果（包括转账通知）已保存
        assertNull(transferMapper.selectById(outerTransfer.getId()));
        PayTransferDO closedTransfer = transferMapper.selectByAppIdAndMerchantOrderId(1L, "1001");
        assertEquals(PayTransferStatusEnum.CLOSED.getStatus(), closedTransfer.getStatus());
        assertEquals("PAYEE_NOT_EXIST", closedTransfer.getChannelErrorCode());
        verify(notifyService).createPayNotifyTask(eq(PayNotifyTypeEnum.TRANSFER.getType()), eq(closedTransfer.getId()));

        // mock 方法（client 再次发起时，转账成功）
        doAnswer(invocation -> {
            PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
            return PayTransferRespDTO.successOf("CHANNEL_T001", LocalDateTime.now(),
                    unifiedReqDTO.getOutTransferNo(), "success");
        }).when(client).unifiedTransfer(any());
        // 调用：使用相同的商户转账编号再次发起
        PayTransferCreateRespDTO respDTO = transactionTemplate.execute(status ->
                transferService.createTransfer(buildTransferCreateReqDTO()));
        // 断言：复用原转账单号
        assertEquals(closedTransfer.getId(), respDTO.getId());
        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(closedTransfer.getNo(), dbTransfer.getNo());
        assertEquals(PayTransferStatusEnum.SUCCESS.getStatus(), dbTransfer.getStatus());
    }

    @Test // 转账关闭后再次发起，返回转账中及新的 package 信息
    public void testCreateTransfer_retryAfterClosed_processing() {
        // mock 方法（client）
        PayClient<?> client = mockPayClient();
        when(client.unifiedTransfer(any())).thenAnswer(invocation -> {
            PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
            PayTransferRespDTO resp = PayTransferRespDTO.processingOf("CHANNEL_T002",
                    unifiedReqDTO.getOutTransferNo(), "processing");
            resp.setChannelPackageInfo("package-info-002");
            return resp;
        });
        // mock 数据：已存在关闭的转账单
        PayTransferDO closedTransfer = insertClosedTransfer();

        // 调用
        PayTransferCreateRespDTO respDTO = transferService.createTransfer(buildTransferCreateReqDTO());
        // 断言：使用新的 package 信息，且不残留上一次的失败原因
        assertEquals(closedTransfer.getId(), respDTO.getId());
        assertEquals("package-info-002", respDTO.getChannelPackageInfo());
        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(PayTransferStatusEnum.PROCESSING.getStatus(), dbTransfer.getStatus());
        assertEquals("package-info-002", dbTransfer.getChannelPackageInfo());
        assertNull(dbTransfer.getChannelErrorCode());
        assertNull(dbTransfer.getChannelErrorMsg());
        assertEquals("CHANNEL_T002", dbTransfer.getChannelTransferNo());
    }

    @Test // 微信转账关闭后再次发起，确认渠道已终态关闭后使用新的转账单号
    public void testCreateTransfer_retryAfterClosed_weixinClosed() {
        PayClient<?> client = mockWeixinPayClient();
        when(client.getTransfer(any())).thenAnswer(invocation ->
                PayTransferRespDTO.closedOf("CANCELLED", "用户超时未确认", invocation.getArgument(0), "closed")
                        .setChannelTransferNo("WX_TRANSFER_001"));
        when(client.unifiedTransfer(any())).thenAnswer(invocation -> {
            PayTransferUnifiedReqDTO unifiedReqDTO = invocation.getArgument(0);
            return PayTransferRespDTO.successOf("WX_TRANSFER_002", LocalDateTime.now(),
                    unifiedReqDTO.getOutTransferNo(), "success");
        });
        PayTransferDO closedTransfer = insertClosedWeixinTransfer();

        PayTransferCreateRespDTO respDTO = transferService.createTransfer(buildWeixinTransferCreateReqDTO());

        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(closedTransfer.getId(), respDTO.getId());
        assertNotEquals(closedTransfer.getNo(), dbTransfer.getNo());
        assertEquals(PayTransferStatusEnum.SUCCESS.getStatus(), dbTransfer.getStatus());
        verify(client).getTransfer(eq(closedTransfer.getNo()));
        ArgumentCaptor<PayTransferUnifiedReqDTO> captor = ArgumentCaptor.forClass(PayTransferUnifiedReqDTO.class);
        verify(client).unifiedTransfer(captor.capture());
        assertEquals(dbTransfer.getNo(), captor.getValue().getOutTransferNo());
    }

    @Test // 微信转账关闭后再次发起，但渠道查询仍为处理中，不允许生成新转账
    public void testCreateTransfer_retryAfterClosed_weixinProcessing() {
        PayClient<?> client = mockWeixinPayClient();
        when(client.getTransfer(any())).thenAnswer(invocation ->
                PayTransferRespDTO.processingOf("WX_TRANSFER_001", invocation.getArgument(0), "processing"));
        PayTransferDO closedTransfer = insertClosedWeixinTransfer();

        assertServiceException(() -> transferService.createTransfer(buildWeixinTransferCreateReqDTO()),
                PAY_TRANSFER_CREATE_FAIL_STATUS_NOT_CLOSED);

        assertEquals(PayTransferStatusEnum.CLOSED.getStatus(), transferMapper.selectById(closedTransfer.getId()).getStatus());
        verify(client, never()).unifiedTransfer(any());
    }

    @Test // 微信转账关闭后再次发起，但渠道查询已成功，不允许再次转账
    public void testCreateTransfer_retryAfterClosed_weixinSuccess() {
        PayClient<?> client = mockWeixinPayClient();
        when(client.getTransfer(any())).thenAnswer(invocation ->
                PayTransferRespDTO.successOf("WX_TRANSFER_001", LocalDateTime.now(),
                        invocation.getArgument(0), "success"));
        PayTransferDO closedTransfer = insertClosedWeixinTransfer();

        assertServiceException(() -> transferService.createTransfer(buildWeixinTransferCreateReqDTO()),
                PAY_TRANSFER_CREATE_FAIL_STATUS_NOT_CLOSED);

        assertEquals(PayTransferStatusEnum.CLOSED.getStatus(), transferMapper.selectById(closedTransfer.getId()).getStatus());
        verify(client, never()).unifiedTransfer(any());
    }

    @Test // 微信转账关闭后再次发起，但渠道查询异常，继续复用原单号并保留待确认状态
    public void testCreateTransfer_retryAfterClosed_weixinQueryException() {
        PayClient<?> client = mockWeixinPayClient();
        when(client.getTransfer(any())).thenThrow(new RuntimeException("模拟查询超时"));
        when(client.unifiedTransfer(any())).thenThrow(new RuntimeException("模拟发起超时"));
        PayTransferDO closedTransfer = insertClosedWeixinTransfer();

        PayTransferCreateRespDTO respDTO = transferService.createTransfer(buildWeixinTransferCreateReqDTO());

        PayTransferDO dbTransfer = transferMapper.selectById(respDTO.getId());
        assertEquals(closedTransfer.getNo(), dbTransfer.getNo());
        assertEquals(PayTransferStatusEnum.WAITING.getStatus(), dbTransfer.getStatus());
        verify(client, times(2)).getTransfer(eq(closedTransfer.getNo()));
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

    private PayClient<?> mockWeixinPayClient() {
        PayAppDO app = randomPojo(PayAppDO.class, o -> o.setId(1L).setTransferNotifyUrl("http://127.0.0.1/transfer"));
        when(appService.validPayApp(eq("demo"))).thenReturn(app);
        PayChannelDO channel = randomPojo(PayChannelDO.class, o -> o.setId(10L).setAppId(1L)
                .setCode(PayChannelEnum.WX_LITE.getCode()));
        when(channelService.validPayChannel(eq(1L), eq(PayChannelEnum.WX_LITE.getCode()))).thenReturn(channel);
        when(payProperties.getTransferNotifyUrl()).thenReturn("http://127.0.0.1");
        PayClient<?> client = mock(PayClient.class);
        when(channelService.getPayClient(eq(10L))).thenReturn(client);
        return client;
    }

    /**
     * 插入关闭的转账单，与 {@link #buildTransferCreateReqDTO()} 的转账请求一致，并残留上一次转账的渠道结果
     *
     * 特殊：渠道编号使用旧的渠道编号，模拟渠道删除重建的情况
     */
    private PayTransferDO insertClosedTransfer() {
        PayTransferDO transfer = randomPojo(PayTransferDO.class, o -> o.setAppId(1L).setChannelId(9L)
                .setChannelCode(PayChannelEnum.ALIPAY_PC.getCode()).setMerchantTransferId("1001").setPrice(100)
                .setUserName("旧的收款人").setChannelExtras(Map.of("sceneId", "1005"))
                .setStatus(PayTransferStatusEnum.CLOSED.getStatus())
                .setChannelErrorCode("PAYEE_NOT_EXIST").setChannelErrorMsg("收款账号不存在")
                .setChannelPackageInfo("package-info-001"));
        transferMapper.insert(transfer);
        return transfer;
    }

    private PayTransferDO insertClosedWeixinTransfer() {
        PayTransferDO transfer = randomPojo(PayTransferDO.class, o -> o.setAppId(1L).setChannelId(10L)
                .setChannelCode(PayChannelEnum.WX_LITE.getCode()).setMerchantTransferId("1001").setPrice(100)
                .setUserName("旧的收款人").setChannelExtras(Map.of("sceneId", "1005"))
                .setStatus(PayTransferStatusEnum.CLOSED.getStatus()).setChannelTransferNo("WX_TRANSFER_001")
                .setChannelErrorCode("CANCELLED").setChannelErrorMsg("用户超时未确认")
                .setChannelPackageInfo("package-info-001"));
        transferMapper.insert(transfer);
        return transfer;
    }

    private static PayTransferCreateReqDTO buildWeixinTransferCreateReqDTO() {
        return buildTransferCreateReqDTO().setChannelCode(PayChannelEnum.WX_LITE.getCode());
    }

    private void updateTransferStatus(String no, Integer status) {
        PayTransferDO transfer = transferMapper.selectByAppIdAndNo(1L, no);
        transferMapper.updateById(new PayTransferDO().setId(transfer.getId()).setStatus(status));
    }

    private static PayTransferCreateReqDTO buildTransferCreateReqDTO() {
        return new PayTransferCreateReqDTO().setAppKey("demo").setChannelCode(PayChannelEnum.ALIPAY_PC.getCode())
                .setUserIp("127.0.0.1").setUserId(1L).setUserType(1)
                .setMerchantTransferId("1001").setSubject("佣金提现").setPrice(100)
                .setUserAccount("test@example.com").setUserName("芋艿").setChannelExtras(Map.of("sceneId", "1000"));
    }

}
