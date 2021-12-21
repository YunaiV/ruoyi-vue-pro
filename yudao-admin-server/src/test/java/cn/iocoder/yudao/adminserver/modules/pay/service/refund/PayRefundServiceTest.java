package cn.iocoder.yudao.adminserver.modules.pay.service.refund;

import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.adminserver.modules.pay.controller.order.vo.refund.vo.PayRefundExportReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.order.vo.refund.vo.PayRefundPageReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.dal.mysql.order.PayRefundMapper;
import cn.iocoder.yudao.adminserver.modules.pay.service.order.impl.PayRefundServiceImpl;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayRefundDO;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayOrderNotifyStatusEnum;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayRefundStatusEnum;
import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayRefundTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * {@link PayRefundServiceImpl} 的单元测试类
 *
 * @author aquan
 */
@Import(PayRefundServiceImpl.class)
public class PayRefundServiceTest extends BaseDbUnitTest {

    @Resource
    private PayRefundServiceImpl refundService;

    @Resource
    private PayRefundMapper refundMapper;


    @Test
    public void testGetRefundPage() {
        // mock 数据
        PayRefundDO dbRefund = randomPojo(PayRefundDO.class, o -> { // 等会查询到
            o.setReqNo("RF0000001");
            o.setMerchantId(1L);
            o.setAppId(1L);
            o.setChannelId(1L);
            o.setChannelCode(PayChannelEnum.WX_PUB.getCode());
            o.setOrderId(1L);
            o.setTradeNo("OT0000001");
            o.setMerchantOrderId("MOT0000001");
            o.setMerchantRefundNo("MRF0000001");
            o.setNotifyUrl("https://www.cancanzi.com");
            o.setNotifyStatus(PayOrderNotifyStatusEnum.SUCCESS.getStatus());
            o.setStatus(PayRefundStatusEnum.SUCCESS.getStatus());
            o.setType(PayRefundTypeEnum.SOME.getStatus());
            o.setPayAmount(100L);
            o.setRefundAmount(500L);
            o.setReason("就是想退款了，你有意见吗");
            o.setUserIp("127.0.0.1");
            o.setChannelOrderNo("CH0000001");
            o.setChannelRefundNo("CHR0000001");
            o.setChannelErrorCode("");
            o.setChannelErrorMsg("");
            o.setChannelExtras("");
            o.setExpireTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 30));
            o.setSuccessTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 15));
            o.setNotifyTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 20));
            o.setCreateTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 10));
            o.setUpdateTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 35));
        });
        refundMapper.insert(dbRefund);
        // 测试 reqNo 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setReqNo("RF1111112")));
        // 测试 merchantId 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setMerchantId(2L)));
        // 测试 appId 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setAppId(2L)));
        // 测试 channelCode 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setChannelCode(PayChannelEnum.ALIPAY_APP.getCode())));
        // 测试 merchantRefundNo 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setMerchantRefundNo("MRF1111112")));
        // 测试 notifyStatus 不匹配
        refundMapper.insert(
                cloneIgnoreId(dbRefund, o -> o.setNotifyStatus(PayOrderNotifyStatusEnum.FAILURE.getStatus())));
        // 测试 status 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setStatus(PayRefundStatusEnum.CLOSE.getStatus())));
        // 测试 type 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setType(PayRefundTypeEnum.ALL.getStatus())));
        // 测试 createTime 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o ->
                o.setCreateTime(DateUtils.buildTime(2022, 1, 1, 10, 10, 10))));
        // 准备参数
        PayRefundPageReqVO reqVO = new PayRefundPageReqVO();
        reqVO.setReqNo("RF0000001");
        reqVO.setMerchantId(1L);
        reqVO.setAppId(1L);
        reqVO.setChannelCode(PayChannelEnum.WX_PUB.getCode());
        reqVO.setMerchantRefundNo("MRF0000001");
        reqVO.setNotifyStatus(PayOrderNotifyStatusEnum.SUCCESS.getStatus());
        reqVO.setStatus(PayRefundStatusEnum.SUCCESS.getStatus());
        reqVO.setType(PayRefundTypeEnum.SOME.getStatus());
        reqVO.setBeginCreateTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 10));
        reqVO.setEndCreateTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 12));


        // 调用
        PageResult<PayRefundDO> pageResult = refundService.getRefundPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbRefund, pageResult.getList().get(0));
    }

    @Test
    public void testGetRefundList() {
        // mock 数据
        PayRefundDO dbRefund = randomPojo(PayRefundDO.class, o -> { // 等会查询到
            o.setReqNo("RF0000001");
            o.setMerchantId(1L);
            o.setAppId(1L);
            o.setChannelId(1L);
            o.setChannelCode(PayChannelEnum.WX_PUB.getCode());
            o.setOrderId(1L);
            o.setTradeNo("OT0000001");
            o.setMerchantOrderId("MOT0000001");
            o.setMerchantRefundNo("MRF0000001");
            o.setNotifyUrl("https://www.cancanzi.com");
            o.setNotifyStatus(PayOrderNotifyStatusEnum.SUCCESS.getStatus());
            o.setStatus(PayRefundStatusEnum.SUCCESS.getStatus());
            o.setType(PayRefundTypeEnum.SOME.getStatus());
            o.setPayAmount(100L);
            o.setRefundAmount(500L);
            o.setReason("就是想退款了，你有意见吗");
            o.setUserIp("127.0.0.1");
            o.setChannelOrderNo("CH0000001");
            o.setChannelRefundNo("CHR0000001");
            o.setChannelErrorCode("");
            o.setChannelErrorMsg("");
            o.setChannelExtras("");
            o.setExpireTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 30));
            o.setSuccessTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 15));
            o.setNotifyTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 20));
            o.setCreateTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 10));
            o.setUpdateTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 35));
        });
        refundMapper.insert(dbRefund);
        // 测试 reqNo 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setReqNo("RF1111112")));
        // 测试 merchantId 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setMerchantId(2L)));
        // 测试 appId 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setAppId(2L)));
        // 测试 channelCode 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setChannelCode(PayChannelEnum.ALIPAY_APP.getCode())));
        // 测试 merchantRefundNo 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setMerchantRefundNo("MRF1111112")));
        // 测试 notifyStatus 不匹配
        refundMapper.insert(
                cloneIgnoreId(dbRefund, o -> o.setNotifyStatus(PayOrderNotifyStatusEnum.FAILURE.getStatus())));
        // 测试 status 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setStatus(PayRefundStatusEnum.CLOSE.getStatus())));
        // 测试 type 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setType(PayRefundTypeEnum.ALL.getStatus())));
        // 测试 createTime 不匹配
        refundMapper.insert(cloneIgnoreId(dbRefund, o ->
                o.setCreateTime(DateUtils.buildTime(2022, 1, 1, 10, 10, 10))));

        // 准备参数
        PayRefundExportReqVO reqVO = new PayRefundExportReqVO();
        reqVO.setReqNo("RF0000001");
        reqVO.setMerchantId(1L);
        reqVO.setAppId(1L);
        reqVO.setChannelCode(PayChannelEnum.WX_PUB.getCode());
        reqVO.setMerchantRefundNo("MRF0000001");
        reqVO.setNotifyStatus(PayOrderNotifyStatusEnum.SUCCESS.getStatus());
        reqVO.setStatus(PayRefundStatusEnum.SUCCESS.getStatus());
        reqVO.setType(PayRefundTypeEnum.SOME.getStatus());
        reqVO.setBeginCreateTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 10));
        reqVO.setEndCreateTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 12));

        // 调用
        List<PayRefundDO> list = refundService.getRefundList(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbRefund, list.get(0));
    }

}
