package cn.iocoder.yudao.module.trade.convert.order;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.member.api.address.dto.AddressRespDTO;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.product.api.property.dto.ProductPropertyValueDetailRespDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateStockReqDTO;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.base.member.user.MemberUserRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.base.product.property.ProductPropertyValueDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderPageItemRespVO;
import cn.iocoder.yudao.module.trade.controller.app.base.property.AppProductPropertyValueDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderPageItemRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.addTime;

@Mapper
public interface TradeOrderConvert {

    TradeOrderConvert INSTANCE = Mappers.getMapper(TradeOrderConvert.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(source = "createReqVO.couponId", target = "couponId"),
            @Mapping(target = "remark", ignore = true),
            @Mapping(source = "createReqVO.remark", target = "userRemark"),
            @Mapping(source = "address.name", target = "receiverName"),
            @Mapping(source = "address.mobile", target = "receiverMobile"),
            @Mapping(source = "address.areaId", target = "receiverAreaId"),
            @Mapping(source = "address.postCode", target = "receiverPostCode"),
            @Mapping(source = "address.detailAddress", target = "receiverDetailAddress"),
    })
    TradeOrderDO convert(Long userId, String userIp, AppTradeOrderCreateReqVO createReqVO,
                         PriceCalculateRespDTO.Order order, AddressRespDTO address);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(source = "sku.spuId", target = "spuId"),
    })
    TradeOrderItemDO convert(PriceCalculateRespDTO.OrderItem orderItem, ProductSkuRespDTO sku);

    default List<TradeOrderItemDO> convertList(TradeOrderDO tradeOrderDO,
                                               List<PriceCalculateRespDTO.OrderItem> orderItems, List<ProductSkuRespDTO> skus) {
        Map<Long, ProductSkuRespDTO> skuMap = convertMap(skus, ProductSkuRespDTO::getId);
        return CollectionUtils.convertList(orderItems, orderItem -> {
            TradeOrderItemDO tradeOrderItemDO = convert(orderItem, skuMap.get(orderItem.getSkuId()));
            tradeOrderItemDO.setOrderId(tradeOrderDO.getId());
            tradeOrderItemDO.setUserId(tradeOrderDO.getUserId());
            tradeOrderItemDO.setAfterSaleStatus(TradeOrderItemAfterSaleStatusEnum.NONE.getStatus()); // 退款信息
//            tradeOrderItemDO.setCommented(false);
            return tradeOrderItemDO;
        });
    }

    @Mapping(source = "userId" , target = "userId")
    PriceCalculateReqDTO convert(AppTradeOrderCreateReqVO createReqVO, Long userId);

    @Mappings({
            @Mapping(source = "skuId", target = "id"),
            @Mapping(source = "count", target = "incrCount"),
    })
    ProductSkuUpdateStockReqDTO.Item convert(TradeOrderItemDO bean);
    List<ProductSkuUpdateStockReqDTO.Item> convertList(List<TradeOrderItemDO> list);

    default PayOrderCreateReqDTO convert(TradeOrderDO tradeOrderDO, List<TradeOrderItemDO> tradeOrderItemDOs,
                                         List<ProductSpuRespDTO> spus, TradeOrderProperties tradeOrderProperties) {
        PayOrderCreateReqDTO createReqDTO = new PayOrderCreateReqDTO()
                .setAppId(tradeOrderProperties.getAppId()).setUserIp(tradeOrderDO.getUserIp());
        // 商户相关字段
        createReqDTO.setMerchantOrderId(String.valueOf(tradeOrderDO.getId()));
        String subject = spus.get(0).getName();
        if (spus.size() > 1) {
            subject += " 等多件";
        }
        createReqDTO.setSubject(subject);
        // 订单相关字段
        createReqDTO.setAmount(tradeOrderDO.getPayPrice()).setExpireTime(addTime(tradeOrderProperties.getExpireTime()));
        return createReqDTO;
    }

    default Set<Long> convertPropertyValueIds(List<TradeOrderItemDO> list) {
        if (CollUtil.isEmpty(list)) {
            return new HashSet<>();
        }
        return list.stream().filter(item -> item.getProperties() != null)
                .flatMap(p -> p.getProperties().stream()) // 遍历多个 Property 属性
                .map(TradeOrderItemDO.Property::getValueId) // 将每个 Property 转换成对应的 propertyId，最后形成集合
                .collect(Collectors.toSet());
    }

    default PageResult<TradeOrderPageItemRespVO> convertPage(PageResult<TradeOrderDO> pageResult, List<TradeOrderItemDO> orderItems,
                                                             List<ProductPropertyValueDetailRespDTO> propertyValueDetails) {
        Map<Long, List<TradeOrderItemDO>> orderItemMap = convertMultiMap(orderItems, TradeOrderItemDO::getOrderId);
        Map<Long, ProductPropertyValueDetailRespDTO> propertyValueDetailMap = convertMap(propertyValueDetails, ProductPropertyValueDetailRespDTO::getValueId);
        // 转化 List
        List<TradeOrderPageItemRespVO> orderVOs = CollectionUtils.convertList(pageResult.getList(), order -> {
            List<TradeOrderItemDO> xOrderItems = orderItemMap.get(order.getId());
            TradeOrderPageItemRespVO orderVO = convert(order, xOrderItems);
            if (CollUtil.isNotEmpty(xOrderItems)) {
                // 处理商品属性
                for (int i = 0; i < xOrderItems.size(); i++) {
                    List<TradeOrderItemDO.Property> properties = xOrderItems.get(i).getProperties();
                    if (CollUtil.isEmpty(properties)) {
                        continue;
                    }
                    TradeOrderPageItemRespVO.Item item = orderVO.getItems().get(i);
                    item.setProperties(new ArrayList<>(properties.size()));
                    // 遍历每个 properties，设置到 TradeOrderPageItemRespVO.Item 中
                    properties.forEach(property -> {
                        ProductPropertyValueDetailRespDTO propertyValueDetail = propertyValueDetailMap.get(property.getValueId());
                        if (propertyValueDetail == null) {
                           return;
                        }
                        item.getProperties().add(convert(propertyValueDetail));
                    });
                }
            }
            // 处理收货地址
            orderVO.setReceiverAreaName(AreaUtils.format(order.getReceiverAreaId()));
            return orderVO;
        });
        return new PageResult<>(orderVOs, pageResult.getTotal());
    }
    TradeOrderPageItemRespVO convert(TradeOrderDO order, List<TradeOrderItemDO> items);
    ProductPropertyValueDetailRespVO convert(ProductPropertyValueDetailRespDTO bean);

    default TradeOrderDetailRespVO convert(TradeOrderDO order, List<TradeOrderItemDO> orderItems,
                                           List<ProductPropertyValueDetailRespDTO> propertyValueDetails, MemberUserRespDTO user) {
        TradeOrderDetailRespVO orderVO = convert2(order, orderItems);
        // 处理商品属性
        Map<Long, ProductPropertyValueDetailRespDTO> propertyValueDetailMap = convertMap(propertyValueDetails, ProductPropertyValueDetailRespDTO::getValueId);
        for (int i = 0; i < orderItems.size(); i++) {
            List<TradeOrderItemDO.Property> properties = orderItems.get(i).getProperties();
            if (CollUtil.isEmpty(properties)) {
                continue;
            }
            TradeOrderDetailRespVO.Item item = orderVO.getItems().get(i);
            item.setProperties(new ArrayList<>(properties.size()));
            // 遍历每个 properties，设置到 TradeOrderPageItemRespVO.Item 中
            properties.forEach(property -> {
                ProductPropertyValueDetailRespDTO propertyValueDetail = propertyValueDetailMap.get(property.getValueId());
                if (propertyValueDetail == null) {
                    return;
                }
                item.getProperties().add(convert(propertyValueDetail));
            });
        }
        // 处理收货地址
        orderVO.setReceiverAreaName(AreaUtils.format(order.getReceiverAreaId()));
        // 处理用户信息
        orderVO.setUser(convert(user));
        return orderVO;
    }
    TradeOrderDetailRespVO convert2(TradeOrderDO order, List<TradeOrderItemDO> items);
    MemberUserRespVO convert(MemberUserRespDTO bean);

    default PageResult<AppTradeOrderPageItemRespVO> convertPage02(PageResult<TradeOrderDO> pageResult, List<TradeOrderItemDO> orderItems,
                                                                  List<ProductPropertyValueDetailRespDTO> propertyValueDetails) {
        Map<Long, List<TradeOrderItemDO>> orderItemMap = convertMultiMap(orderItems, TradeOrderItemDO::getOrderId);
        Map<Long, ProductPropertyValueDetailRespDTO> propertyValueDetailMap = convertMap(propertyValueDetails, ProductPropertyValueDetailRespDTO::getValueId);
        // 转化 List
        List<AppTradeOrderPageItemRespVO> orderVOs = CollectionUtils.convertList(pageResult.getList(), order -> {
            List<TradeOrderItemDO> xOrderItems = orderItemMap.get(order.getId());
            AppTradeOrderPageItemRespVO orderVO = convert02(order, xOrderItems);
            if (CollUtil.isNotEmpty(xOrderItems)) {
                // 处理商品属性
                for (int i = 0; i < xOrderItems.size(); i++) {
                    List<TradeOrderItemDO.Property> properties = xOrderItems.get(i).getProperties();
                    if (CollUtil.isEmpty(properties)) {
                        continue;
                    }
                    AppTradeOrderPageItemRespVO.Item item = orderVO.getItems().get(i);
                    item.setProperties(new ArrayList<>(properties.size()));
                    // 遍历每个 properties，设置到 TradeOrderPageItemRespVO.Item 中
                    properties.forEach(property -> {
                        ProductPropertyValueDetailRespDTO propertyValueDetail = propertyValueDetailMap.get(property.getValueId());
                        if (propertyValueDetail == null) {
                            return;
                        }
                        item.getProperties().add(convert02(propertyValueDetail));
                    });
                }
            }
            return orderVO;
        });
        return new PageResult<>(orderVOs, pageResult.getTotal());
    }
    AppTradeOrderPageItemRespVO convert02(TradeOrderDO order, List<TradeOrderItemDO> items);
    AppProductPropertyValueDetailRespVO convert02(ProductPropertyValueDetailRespDTO bean);

    default AppTradeOrderDetailRespVO convert02(TradeOrderDO order, List<TradeOrderItemDO> orderItems,
                                                List<ProductPropertyValueDetailRespDTO> propertyValueDetails) {
        AppTradeOrderDetailRespVO orderVO = convert3(order, orderItems);
        // 处理商品属性
        Map<Long, ProductPropertyValueDetailRespDTO> propertyValueDetailMap = convertMap(propertyValueDetails, ProductPropertyValueDetailRespDTO::getValueId);
        for (int i = 0; i < orderItems.size(); i++) {
            List<TradeOrderItemDO.Property> properties = orderItems.get(i).getProperties();
            if (CollUtil.isEmpty(properties)) {
                continue;
            }
            AppTradeOrderDetailRespVO.Item item = orderVO.getItems().get(i);
            item.setProperties(new ArrayList<>(properties.size()));
            // 遍历每个 properties，设置到 TradeOrderPageItemRespVO.Item 中
            properties.forEach(property -> {
                ProductPropertyValueDetailRespDTO propertyValueDetail = propertyValueDetailMap.get(property.getValueId());
                if (propertyValueDetail == null) {
                    return;
                }
                item.getProperties().add(convert02(propertyValueDetail));
            });
        }
        // 处理收货地址
        orderVO.setReceiverAreaName(AreaUtils.format(order.getReceiverAreaId()));
        return orderVO;
    }
    AppTradeOrderDetailRespVO convert3(TradeOrderDO order, List<TradeOrderItemDO> items);

}
