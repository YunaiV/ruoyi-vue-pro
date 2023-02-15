package cn.iocoder.yudao.module.pay.service.demo;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.api.refund.PayRefundApi;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.demo.vo.PayDemoOrderCreateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoOrderDO;
import cn.iocoder.yudao.module.pay.dal.mysql.demo.PayDemoOrderMapper;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.addTime;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;

/**
 * 示例订单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class PayDemoOrderServiceImpl implements PayDemoOrderService {

    /**
     * 接入的实力应用编号
     *
     * 从 [支付管理 -> 应用信息] 里添加
     */
    private static final Long PAY_APP_ID = 7L;

    /**
     * 商品信息 Map
     *
     * key：商品编号
     * value：[商品名、商品价格]
     */
    private final Map<Long, Object[]> spuNames = new HashMap<>();

    @Resource
    private PayOrderApi payOrderApi;
    @Resource
    private PayRefundApi payRefundApi;

    @Resource
    private PayDemoOrderMapper payDemoOrderMapper;

    public PayDemoOrderServiceImpl() {
        spuNames.put(1L, new Object[]{"华为手机", 1});
        spuNames.put(2L, new Object[]{"小米电视", 10});
        spuNames.put(3L, new Object[]{"苹果手表", 100});
        spuNames.put(4L, new Object[]{"华硕笔记本", 200});
        spuNames.put(5L, new Object[]{"蔚来汽车", 300});
    }

    @Override
    public Long createDemoOrder(Long userId, PayDemoOrderCreateReqVO createReqVO) {
        // 1.1 获得商品
        Object[] spu = spuNames.get(createReqVO.getSpuId());
        Assert.notNull(spu, "商品({}) 不存在", createReqVO.getSpuId());
        String spuName = (String) spu[0];
        Integer price = (Integer) spu[1];
        // 1.2 插入 demo 订单
        PayDemoOrderDO demoOrder = new PayDemoOrderDO().setUserId(userId)
                .setSpuId(createReqVO.getSpuId()).setSpuName(spuName)
                .setPrice(price).setPayed(false).setRefundPrice(0);
        payDemoOrderMapper.insert(demoOrder);

        // 2.1 创建支付单
        Long payOrderId = payOrderApi.createOrder(new PayOrderCreateReqDTO()
                .setAppId(PAY_APP_ID).setUserIp(getClientIP()) // 支付应用
                .setMerchantOrderId(demoOrder.getId().toString()) // 业务的订单编号
                .setSubject(spuName).setBody("").setAmount(price) // 价格信息
                .setExpireTime(addTime(Duration.ofHours(2L)))); // 支付的过期时间
        // 2.2 更新支付单到 demo 订单
        payDemoOrderMapper.updateById(new PayDemoOrderDO().setId(demoOrder.getId())
                .setPayOrderId(payOrderId));
        // 返回
        return demoOrder.getId();
    }

//    private void validateDemoOrderExists(Long id) {
//        if (demoOrderMapper.selectById(id) == null) {
//            throw exception(DEMO_ORDER_NOT_EXISTS);
//        }
//    }

    @Override
    public PayDemoOrderDO getDemoOrder(Long id) {
        return payDemoOrderMapper.selectById(id);
    }

    @Override
    public PageResult<PayDemoOrderDO> getDemoOrderPage(PageParam pageReqVO) {
        return payDemoOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateDemoOrderPaid(Long id, Long payOrderId) {
        // 校验并获得支付订单（可支付）
        PayOrderRespDTO payOrder = validateDemoOrderCanPaid(id, payOrderId);

        // 更新 PayDemoOrderDO 状态为已支付
        int updateCount = payDemoOrderMapper.updateByIdAndPayed(id, false,
                new PayDemoOrderDO().setPayed(true).setPayTime(LocalDateTime.now())
                        .setPayChannelCode(payOrder.getChannelCode()));
        if (updateCount == 0) {
            throw exception(PAY_DEMO_ORDER_UPDATE_PAID_STATUS_NOT_UNPAID);
        }
    }

    /**
     * 校验交易订单满足被支付的条件
     *
     * 1. 交易订单未支付
     * 2. 支付单已支付
     *
     * @param id 交易订单编号
     * @param payOrderId 支付订单编号
     * @return 交易订单
     */
    private PayOrderRespDTO validateDemoOrderCanPaid(Long id, Long payOrderId) {
        // 校验订单是否存在
        PayDemoOrderDO order = payDemoOrderMapper.selectById(id);
        if (order == null) {
            throw exception(PAY_DEMO_ORDER_NOT_FOUND);
        }
        // 校验订单未支付
        if (order.getPayed()) {
            log.error("[validateDemoOrderCanPaid][order({}) 不处于待支付状态，请进行处理！order 数据是：{}]",
                    id, JsonUtils.toJsonString(order));
            throw exception(PAY_DEMO_ORDER_UPDATE_PAID_STATUS_NOT_UNPAID);
        }
        // 校验支付订单匹配
        if (ObjectUtil.notEqual(order.getPayOrderId(), payOrderId)) { // 支付单号
            log.error("[validateDemoOrderCanPaid][order({}) 支付单不匹配({})，请进行处理！order 数据是：{}]",
                    id, payOrderId, JsonUtils.toJsonString(order));
            throw exception(PAY_DEMO_ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        }

        // 校验支付单是否存在
        PayOrderRespDTO payOrder = payOrderApi.getOrder(payOrderId);
        if (payOrder == null) {
            log.error("[validateDemoOrderCanPaid][order({}) payOrder({}) 不存在，请进行处理！]", id, payOrderId);
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        // 校验支付单已支付
        if (!PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            log.error("[validateDemoOrderCanPaid][order({}) payOrder({}) 未支付，请进行处理！payOrder 数据是：{}]",
                    id, payOrderId, JsonUtils.toJsonString(payOrder));
            throw exception(PAY_DEMO_ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS);
        }
        // 校验支付金额一致
        if (ObjectUtil.notEqual(payOrder.getAmount(), order.getPrice())) {
            log.error("[validateDemoOrderCanPaid][order({}) payOrder({}) 支付金额不匹配，请进行处理！order 数据是：{}，payOrder 数据是：{}]",
                    id, payOrderId, JsonUtils.toJsonString(order), JsonUtils.toJsonString(payOrder));
            throw exception(PAY_DEMO_ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH);
        }
        // 校验支付订单匹配（二次）
        if (ObjectUtil.notEqual(payOrder.getMerchantOrderId(), id.toString())) {
            log.error("[validateDemoOrderCanPaid][order({}) 支付单不匹配({})，请进行处理！payOrder 数据是：{}]",
                    id, payOrderId, JsonUtils.toJsonString(payOrder));
            throw exception(PAY_DEMO_ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        }
        return payOrder;
    }

    @Override
    public void refundDemoOrder(Long id, String userIp) {
        // 1. 校验订单是否可以退款
        PayDemoOrderDO order = validateDemoOrderCanRefund(id);

        // 2.1 创建退款单
        Long payRefundId = payRefundApi.createPayRefund(new PayRefundCreateReqDTO()
                .setAppId(PAY_APP_ID).setUserIp(getClientIP()) // 支付应用
                .setMerchantOrderId(order.getId().toString()) // 业务的订单编号
                .setReason("想退钱").setAmount(order.getPrice())); // 价格信息
        // 2.2 更新支付单到 demo 订单
        payDemoOrderMapper.updateById(new PayDemoOrderDO().setId(id)
                .setPayRefundId(payRefundId).setRefundPrice(order.getPrice()));
    }

    private PayDemoOrderDO validateDemoOrderCanRefund(Long id) {
        // 校验订单是否存在
        PayDemoOrderDO order = payDemoOrderMapper.selectById(id);
        if (order == null) {
            throw exception(PAY_DEMO_ORDER_NOT_FOUND);
        }
        // 校验订单是否支付
        if (!order.getPayed()) {
            throw exception(PAY_DEMO_ORDER_REFUND_FAIL_NOT_PAID);
        }
        // 校验是否已经发起退款
        if (order.getPayRefundId() != null) {
            throw exception(PAY_DEMO_ORDER_REFUND_FAIL_REFUNDED);
        }
        return order;
    }

}
