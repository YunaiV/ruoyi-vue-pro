package cn.iocoder.yudao.module.oms.job;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.oms.api.enums.order.OrderStatusEnum;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderItemDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductDO;
import cn.iocoder.yudao.module.oms.service.OmsOrderItemService;
import cn.iocoder.yudao.module.oms.service.OmsOrderService;
import cn.iocoder.yudao.module.oms.service.OmsShopProductService;
import cn.iocoder.yudao.module.oms.service.OmsShopService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 根据店铺产品信息填充订单项shopProductId字段
 */
@Slf4j
@Component
public class OmsFillOrderItemFieldJob implements JobHandler {


    @Resource
    private OmsShopService omsShopService;
    @Resource
    private OmsShopProductService omsShopProductService;
    @Resource
    private OmsOrderService omsOrderService;
    @Resource
    private OmsOrderItemService omsOrderItemService;

    @TenantJob
    @Override
    public String execute(String param) {
        //找出该平台下所有的店铺
        Map<String, OmsShopDO> omsShopMap = omsShopService.getByPlatformCode(param)
            .stream().collect(Collectors.toMap(OmsShopDO::getExternalId, Function.identity()));
        //找出该平台下所有的订单
        List<OmsOrderDO> existOrders = omsOrderService.getByPlatformCode(param);

        //使用Map存储已存在的订单，key = orderId, value = OmsOrderDO
        Map<Long, OmsOrderDO> existOrderIdMap = Optional.ofNullable(existOrders)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(OmsOrderDO::getId, omsOrderDO -> omsOrderDO));
        List<Long> existOrderIds = existOrders.stream().map(OmsOrderDO::getId).toList();
        //找出该平台下所有已存在的订单项
        List<OmsOrderItemDO> existOrderItems = omsOrderService.getOrderItemListByOrderIds(existOrderIds);

        List<Long> shopIds = omsShopMap.values().stream().map(OmsShopDO::getId).toList();
        //找出该平台下所有已激活的店铺产品
        List<OmsShopProductDO> existShopProducts = omsShopProductService.getByShopIds(shopIds).stream()
            .filter(omsShopProductDTO -> omsShopProductDTO.getStatus() == true)
            .toList();

        //key=externalId,value=OmsShopProductDO
        Map<String, OmsShopProductDO> existShopProductMap = existShopProducts.stream()
            .collect(Collectors.toMap(OmsShopProductDO::getExternalId, Function.identity()));


        // 使用Map存储已存在的店铺产品信息，key=shopId, value=Map<String, OmsShopProductDO>,其中key=code即平台sku
        Map<Long, Map<String, OmsShopProductDO>> productsByShopIdAndCode = existShopProducts.stream()
            .collect(Collectors.groupingBy(
                OmsShopProductDO::getShopId,
                Collectors.toMap(
                    OmsShopProductDO::getCode,
                    product -> product
                )
            ));

        List<OmsOrderDO> updateOrders = new ArrayList<>();

        for (OmsOrderItemDO existOrderItem : existOrderItems) {

            //如果订单项里有shopProductExternalId，用这个唯一标识可以直接匹配店铺产品
            if (StrUtil.isNotEmpty(existOrderItem.getShopProductExternalId())) {
                OmsShopProductDO shopProduct = existShopProductMap.get(existOrderItem.getShopProductExternalId());
                existOrderItem.setShopProductId(shopProduct.getId());
                MapUtils.findAndThen(existOrderIdMap, existOrderItem.getOrderId(), omsOrderDO -> {
                    omsOrderDO.setStatus(OrderStatusEnum.PENDING_REVIEW.getType());
                    updateOrders.add(omsOrderDO);
                });
            }
            //店铺 + 店铺产品外部编码shopProductExternalCode + 激活状态，匹配唯一店铺产品
            else {
                OmsOrderDO omsOrderDO = existOrderIdMap.get(existOrderItem.getOrderId());
                Long shopId = omsOrderDO.getShopId();
                Map<String, OmsShopProductDO> stringOmsShopProductDOMap = productsByShopIdAndCode.get(shopId);
                //当订单项所对应的店铺产品不存在或没有激活时，跳过该订单项
                if (MapUtil.isEmpty(stringOmsShopProductDOMap)) {
                    continue;
                }
                OmsShopProductDO omsShopProductDO = stringOmsShopProductDOMap.get(existOrderItem.getShopProductExternalCode());
                existOrderItem.setShopProductId(omsShopProductDO.getId());
                omsOrderDO.setStatus(OrderStatusEnum.PENDING_REVIEW.getType());
                updateOrders.add(omsOrderDO);
            }
        }
        omsOrderService.updateOrders(updateOrders);
        omsOrderItemService.updateOrderItems(existOrderItems);
        log.info("Fill OrderItem Field Success!");
        return "";
    }
}
