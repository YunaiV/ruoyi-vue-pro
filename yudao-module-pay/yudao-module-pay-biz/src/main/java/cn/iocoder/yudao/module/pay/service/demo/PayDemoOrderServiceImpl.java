package cn.iocoder.yudao.module.pay.service.demo;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.demo.vo.PayDemoOrderCreateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoOrderDO;
import cn.iocoder.yudao.module.pay.dal.mysql.demo.PayDemoOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.addTime;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;

/**
 * 示例订单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
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
                .setPayed(false).setRefundPrice(0);
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

}
