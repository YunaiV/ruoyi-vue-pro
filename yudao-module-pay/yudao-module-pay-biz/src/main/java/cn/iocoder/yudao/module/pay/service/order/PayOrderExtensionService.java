package cn.iocoder.yudao.module.pay.service.order;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 支付订单 Service 接口
 *
 * @author aquan
 */
public interface PayOrderExtensionService {

    /**
     * 获得支付订单
     *
     * @param id 编号
     * @return 支付订单
     */
    PayOrderExtensionDO getOrderExtension(Long id);

    /**
     * 获得支付订单
     * 列表
     *
     * @param ids 编号
     * @return 支付订单
     * 列表
     */
    List<PayOrderExtensionDO> getOrderExtensionList(Collection<Long> ids);

    /**
     * 根据订单成功的 扩展订单ID 查询所有的扩展订单转 成 map 返回
     *
     * @param successExtensionIdList 订单 ID 集合
     * @return 订单扩展 map 集合
     */
    default Map<Long, PayOrderExtensionDO> getOrderExtensionMap(Collection<Long> successExtensionIdList) {
        List<PayOrderExtensionDO> list = getOrderExtensionList(successExtensionIdList);
        return CollectionUtils.convertMap(list, PayOrderExtensionDO::getId);
    }

}
