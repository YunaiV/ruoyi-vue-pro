package cn.iocoder.yudao.module.pay.service.transfer;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import cn.iocoder.yudao.module.pay.dal.mysql.transfer.PayTransferMapper;
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum;
import cn.iocoder.yudao.module.pay.framework.pay.config.PayProperties;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.transfer.PayTransferRespDTO;
import cn.iocoder.yudao.module.pay.service.app.PayAppService;
import cn.iocoder.yudao.module.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
