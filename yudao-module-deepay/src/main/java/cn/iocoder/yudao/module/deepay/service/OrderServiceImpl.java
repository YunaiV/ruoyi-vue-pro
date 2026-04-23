package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayMetricsDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayMetricsMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayOrderMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单服务实现。
 *
 * <p>支付流转：INIT → PAID，同步更新商品状态 SELLING → SOLD，写入复盘指标。</p>
 */
@Service
public class OrderServiceImpl implements OrderService {

    /** 默认商品价格（成本 100 × 2.5，与 PricingAgent 保持一致） */
    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("250.00");

    @Resource
    private DeepayOrderMapper orderMapper;

    @Resource
    private DeepayProductMapper productMapper;

    @Resource
    private DeepayMetricsMapper metricsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeepayOrderDO createOrder(String chainCode) {
        // 查商品价格，没有商品时用默认价
        DeepayProductDO product = productMapper.selectByChainCode(chainCode);
        BigDecimal amount = (product != null && product.getPrice() != null)
                ? product.getPrice() : DEFAULT_PRICE;

        // 创建订单
        DeepayOrderDO order = new DeepayOrderDO();
        order.setChainCode(chainCode);
        order.setAmount(amount);
        order.setStatus("INIT");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        // 写入 metrics 初始记录（sold=0）
        DeepayMetricsDO metrics = new DeepayMetricsDO();
        metrics.setChainCode(chainCode);
        metrics.setPrice(amount);
        metrics.setSold(0);
        metrics.setCreatedAt(LocalDateTime.now());
        metricsMapper.insert(metrics);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeepayOrderDO markPaid(String chainCode) {
        // 更新订单状态
        orderMapper.updateStatusByChainCode(chainCode, "PAID");

        // 更新商品状态
        productMapper.updateStatusByChainCode(chainCode, "SOLD");

        // 更新 metrics（sold=1）
        metricsMapper.markSoldByChainCode(chainCode);

        return orderMapper.selectByChainCode(chainCode);
    }

    @Override
    public DeepayProductDO getProduct(String chainCode) {
        return productMapper.selectByChainCode(chainCode);
    }

}
