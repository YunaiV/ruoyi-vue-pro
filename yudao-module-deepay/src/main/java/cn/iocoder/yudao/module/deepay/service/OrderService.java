package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;

/**
 * 订单服务接口。
 *
 * <ul>
 *   <li>创建订单（INIT）</li>
 *   <li>模拟支付 / 接收 Webhook，将订单标记为 PAID，商品标记为 SOLD，写入 metrics</li>
 * </ul>
 */
public interface OrderService {

    /**
     * 根据 chainCode 创建订单（INIT），同时写入 metrics 初始记录。
     *
     * @param chainCode 商品链码
     * @return 新建订单
     */
    DeepayOrderDO createOrder(String chainCode);

    /**
     * 将订单标记为已支付（INIT → PAID），同步更新商品状态（SELLING → SOLD）并更新 metrics。
     *
     * @param chainCode 商品链码
     * @return 更新后的订单
     */
    DeepayOrderDO markPaid(String chainCode);

    /**
     * 根据 chainCode 查询商品信息（用于商品详情页）。
     *
     * @param chainCode 商品链码
     * @return 商品 DO，不存在时返回 null
     */
    DeepayProductDO getProduct(String chainCode);

}
