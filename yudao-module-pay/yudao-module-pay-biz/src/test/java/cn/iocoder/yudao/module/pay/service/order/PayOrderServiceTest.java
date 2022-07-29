package cn.iocoder.yudao.module.pay.service.order;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.pay.config.PayProperties;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.mysql.order.PayOrderMapper;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderNotifyStatusEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundTypeEnum;
import cn.iocoder.yudao.module.pay.service.merchant.PayAppService;
import cn.iocoder.yudao.module.pay.service.merchant.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link PayOrderServiceImpl} 的单元测试类
 *
 * @author 芋艿
 */
@Import({PayOrderServiceImpl.class})
public class PayOrderServiceTest extends BaseDbUnitTest {

    @Resource
    private PayOrderServiceImpl orderService;

    @Resource
    private PayOrderMapper orderMapper;

    @MockBean
    private PayClientFactory payClientFactory;
    @MockBean
    private PayProperties properties;
    @MockBean
    private PayAppService appService;
    @MockBean
    private PayChannelService channelService;
    @MockBean
    private PayNotifyService notifyService;

    public String generateNo() {
        return DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomInt(100000, 999999);
    }

    @Test
    public void testGetOrderPage() {

        String merchantOrderId = generateNo();
        String channelOrderId = generateNo();

        // mock 数据
        PayOrderDO dbOrder = randomPojo(PayOrderDO.class, o -> { // 等会查询到
            o.setMerchantId(1L);
            o.setAppId(1L);
            o.setChannelId(1L);
            o.setChannelCode(PayChannelEnum.WX_PUB.getCode());
            o.setMerchantOrderId(merchantOrderId);
            o.setSubject("灿灿子的炸弹猫");
            o.setBody("斌斌子送给灿灿子的炸弹猫");
            o.setNotifyUrl("https://hc.com/lbh");
            o.setNotifyStatus(PayOrderNotifyStatusEnum.SUCCESS.getStatus());
            o.setAmount(10000L);
            o.setChannelFeeRate(0.01);
            o.setChannelFeeAmount(1L);
            o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
            o.setUserIp("127.0.0.1");
            o.setCreateTime(DateUtils.buildTime(2018, 1, 1, 10, 1, 0));
            o.setExpireTime(DateUtils.buildTime(2018, 1, 1, 10, 30, 0));
            o.setSuccessTime(DateUtils.buildTime(2018, 1, 1, 10, 10, 2));
            o.setNotifyTime(DateUtils.buildTime(2018, 1, 1, 10, 10, 15));
            o.setSuccessExtensionId(1L);
            o.setRefundStatus(PayRefundTypeEnum.NO.getStatus());
            o.setRefundTimes(0);
            o.setRefundAmount(0L);
            o.setChannelUserId("1008611");
            o.setChannelOrderNo(channelOrderId);
            o.setUpdateTime(DateUtils.buildTime(2018, 1, 1, 10, 10, 15));
        });
        orderMapper.insert(dbOrder);
        // 测试 merchantId 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setMerchantId(2L)));
        // 测试 appId 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setAppId(2L)));
        // 测试 channelId 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setChannelId(2L)));
        // 测试 channelCode 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setChannelCode(PayChannelEnum.ALIPAY_APP.getCode())));
        // 测试 merchantOrderId 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setMerchantOrderId(generateNo())));
        // 测试 notifyStatus 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setNotifyStatus(PayOrderNotifyStatusEnum.FAILURE.getStatus())));
        // 测试 status 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setStatus(PayOrderStatusEnum.CLOSED.getStatus())));
        // 测试 refundStatus 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setRefundStatus(PayRefundTypeEnum.ALL.getStatus())));
        // 测试 createTime 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setCreateTime(DateUtils.buildTime(2019, 1, 1, 10, 10,
                1))));
        // 准备参数
        PayOrderPageReqVO reqVO = new PayOrderPageReqVO();
        reqVO.setMerchantId(1L);
        reqVO.setAppId(1L);
        reqVO.setChannelId(1L);
        reqVO.setChannelCode(PayChannelEnum.WX_PUB.getCode());
        reqVO.setMerchantOrderId(merchantOrderId);
        reqVO.setNotifyStatus(PayOrderNotifyStatusEnum.SUCCESS.getStatus());
        reqVO.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
        reqVO.setRefundStatus(PayRefundTypeEnum.NO.getStatus());
        reqVO.setCreateTime((new Date[]{buildTime(2018, 1, 1, 10, 1, 0),buildTime(2018, 1, 1, 10, 1, 0)}));
        // 调用
        PageResult<PayOrderDO> pageResult = orderService.getOrderPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbOrder, pageResult.getList().get(0));
        // assertEquals(0, dbOrder.getUpdateTime().compareTo(pageResult.getList().get(0).getUpdateTime()));
    }

    @Test
    public void testGetOrderList() {
        // mock 数据
        String merchantOrderId = generateNo();
        String channelOrderId = generateNo();
        PayOrderDO dbOrder = randomPojo(PayOrderDO.class, o -> { // 等会查询到
            o.setMerchantId(1L);
            o.setAppId(1L);
            o.setChannelId(1L);
            o.setChannelCode(PayChannelEnum.WX_PUB.getCode());
            o.setMerchantOrderId(merchantOrderId);
            o.setSubject("灿灿子的炸弹猫");
            o.setBody("斌斌子送给灿灿子的炸弹猫");
            o.setNotifyUrl("https://hc.com/lbh");
            o.setNotifyStatus(PayOrderNotifyStatusEnum.SUCCESS.getStatus());
            o.setAmount(10000L);
            o.setChannelFeeRate(0.01);
            o.setChannelFeeAmount(1L);
            o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
            o.setUserIp("127.0.0.1");
            o.setCreateTime(DateUtils.buildTime(2018, 1, 1, 10, 1, 0));
            o.setExpireTime(DateUtils.buildTime(2018, 1, 1, 10, 30, 0));
            o.setSuccessTime(DateUtils.buildTime(2018, 1, 1, 10, 10, 2));
            o.setNotifyTime(DateUtils.buildTime(2018, 1, 1, 10, 10, 15));
            o.setSuccessExtensionId(1L);
            o.setRefundStatus(PayRefundTypeEnum.NO.getStatus());
            o.setRefundTimes(0);
            o.setRefundAmount(0L);
            o.setChannelUserId("1008611");
            o.setChannelOrderNo(channelOrderId);
            o.setUpdateTime(DateUtils.buildTime(2018, 1, 1, 10, 10, 15));

        });
        orderMapper.insert(dbOrder);
        // 测试 merchantId 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setMerchantId(2L)));
        // 测试 appId 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setAppId(2L)));
        // 测试 channelId 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setChannelId(2L)));
        // 测试 channelCode 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setChannelCode(PayChannelEnum.ALIPAY_APP.getCode())));
        // 测试 merchantOrderId 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setMerchantOrderId(generateNo())));
        // 测试 notifyStatus 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setNotifyStatus(PayOrderNotifyStatusEnum.FAILURE.getStatus())));
        // 测试 status 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setStatus(PayOrderStatusEnum.CLOSED.getStatus())));
        // 测试 refundStatus 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setRefundStatus(PayRefundTypeEnum.ALL.getStatus())));
        // 测试 createTime 不匹配
        orderMapper.insert(cloneIgnoreId(dbOrder, o -> o.setCreateTime(DateUtils.buildTime(2019, 1, 1, 10, 10,
                1))));
        // 准备参数
        PayOrderExportReqVO reqVO = new PayOrderExportReqVO();
        reqVO.setMerchantId(1L);
        reqVO.setAppId(1L);
        reqVO.setChannelId(1L);
        reqVO.setChannelCode(PayChannelEnum.WX_PUB.getCode());
        reqVO.setMerchantOrderId(merchantOrderId);
        reqVO.setNotifyStatus(PayOrderNotifyStatusEnum.SUCCESS.getStatus());
        reqVO.setStatus(PayOrderStatusEnum.SUCCESS.getStatus());
        reqVO.setRefundStatus(PayRefundTypeEnum.NO.getStatus());
        reqVO.setCreateTime((new Date[]{buildTime(2018, 1, 1, 10, 1, 0),buildTime(2018, 1, 1, 10, 1, 0)}));

        // 调用
        List<PayOrderDO> list = orderService.getOrderList(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbOrder, list.get(0));
    }

}
