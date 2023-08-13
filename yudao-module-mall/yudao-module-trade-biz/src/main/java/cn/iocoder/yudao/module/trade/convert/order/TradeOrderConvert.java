package cn.iocoder.yudao.module.trade.convert.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.dict.core.util.DictFrameworkUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.member.api.address.dto.AddressRespDTO;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.enums.DictTypeConstants;
import cn.iocoder.yudao.module.product.api.comment.dto.ProductCommentCreateReqDTO;
import cn.iocoder.yudao.module.product.api.property.dto.ProductPropertyValueDetailRespDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateStockReqDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.trade.api.order.dto.TradeOrderRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.base.member.user.MemberUserRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.base.product.property.ProductPropertyValueDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderPageItemRespVO;
import cn.iocoder.yudao.module.trade.controller.app.base.property.AppProductPropertyValueDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.*;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.item.AppTradeOrderItemCommentCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.item.AppTradeOrderItemRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.TradeCartDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
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
            @Mapping(source = "calculateRespBO.price.totalPrice", target = "totalPrice"),
            @Mapping(source = "calculateRespBO.price.discountPrice", target = "discountPrice"),
            @Mapping(source = "calculateRespBO.price.deliveryPrice", target = "deliveryPrice"),
            @Mapping(source = "calculateRespBO.price.couponPrice", target = "couponPrice"),
            @Mapping(source = "calculateRespBO.price.pointPrice", target = "pointPrice"),
            @Mapping(source = "calculateRespBO.price.payPrice", target = "payPrice"),
            @Mapping(source = "address.name", target = "receiverName"),
            @Mapping(source = "address.mobile", target = "receiverMobile"),
            @Mapping(source = "address.areaId", target = "receiverAreaId"),
            @Mapping(source = "address.detailAddress", target = "receiverDetailAddress"),
    })
    TradeOrderDO convert(Long userId, String userIp, AppTradeOrderCreateReqVO createReqVO,
                         TradePriceCalculateRespBO calculateRespBO, AddressRespDTO address);

    TradeOrderRespDTO convert(TradeOrderDO orderDO);

    default List<TradeOrderItemDO> convertList(TradeOrderDO tradeOrderDO, TradePriceCalculateRespBO calculateRespBO) {
        return CollectionUtils.convertList(calculateRespBO.getItems(), item -> {
            TradeOrderItemDO orderItem = convert(item);
            orderItem.setOrderId(tradeOrderDO.getId());
            orderItem.setUserId(tradeOrderDO.getUserId());
            orderItem.setAfterSaleStatus(TradeOrderItemAfterSaleStatusEnum.NONE.getStatus());
            orderItem.setCommentStatus(false);
            return orderItem;
        });
    }

    TradeOrderItemDO convert(TradePriceCalculateRespBO.OrderItem item);

    @Mapping(source = "userId", target = "userId")
    PriceCalculateReqDTO convert(AppTradeOrderCreateReqVO createReqVO, Long userId);

    @Mappings({
            @Mapping(source = "skuId", target = "id"),
            @Mapping(source = "count", target = "incrCount"),
    })
    ProductSkuUpdateStockReqDTO.Item convert(TradeOrderItemDO bean);

    List<ProductSkuUpdateStockReqDTO.Item> convertList(List<TradeOrderItemDO> list);

    default PayOrderCreateReqDTO convert(TradeOrderDO order, List<TradeOrderItemDO> orderItems,
                                         TradePriceCalculateRespBO calculateRespBO, TradeOrderProperties orderProperties) {
        PayOrderCreateReqDTO createReqDTO = new PayOrderCreateReqDTO()
                .setAppId(orderProperties.getAppId()).setUserIp(order.getUserIp());
        // 商户相关字段
        createReqDTO.setMerchantOrderId(String.valueOf(order.getId()));
        String subject = calculateRespBO.getItems().get(0).getSpuName();
        if (calculateRespBO.getItems().size() > 1) {
            subject += " 等多件";
        }
        createReqDTO.setSubject(subject);
        createReqDTO.setBody(subject); // TODO 芋艿：临时写死
        // 订单相关字段
        createReqDTO.setPrice(order.getPayPrice()).setExpireTime(addTime(orderProperties.getExpireTime()));
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

    // TODO 芋艿：可简化
    default PageResult<TradeOrderPageItemRespVO> convertPage(PageResult<TradeOrderDO> pageResult, List<TradeOrderItemDO> orderItems,
                                                             List<ProductPropertyValueDetailRespDTO> propertyValueDetails,
                                                             Map<Long, MemberUserRespDTO> memberUserRespDTOMap) {
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
            // 增加用户昵称
            orderVO.setUser(memberUserRespDTOMap.get(orderVO.getUserId()));
            return orderVO;
        });
        return new PageResult<>(orderVOs, pageResult.getTotal());
    }

    TradeOrderPageItemRespVO convert(TradeOrderDO order, List<TradeOrderItemDO> items);

    ProductPropertyValueDetailRespVO convert(ProductPropertyValueDetailRespDTO bean);

    // TODO 芋艿：可简化
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

    // TODO 芋艿：可简化
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
                    AppTradeOrderItemRespVO item = orderVO.getItems().get(i);
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

    // TODO 芋艿：可简化
    default AppTradeOrderDetailRespVO convert02(TradeOrderDO order, List<TradeOrderItemDO> orderItems,
                                                List<ProductPropertyValueDetailRespDTO> propertyValueDetails, TradeOrderProperties tradeOrderProperties,
                                                DeliveryExpressDO express) {
        AppTradeOrderDetailRespVO orderVO = convert3(order, orderItems);
        orderVO.setPayExpireTime(addTime(tradeOrderProperties.getExpireTime()));
        if (StrUtil.isNotEmpty(order.getPayChannelCode())) {
            orderVO.setPayChannelName(DictFrameworkUtils.getDictDataLabel(DictTypeConstants.CHANNEL_CODE, order.getPayChannelCode()));
        }
        // 处理商品属性
        Map<Long, ProductPropertyValueDetailRespDTO> propertyValueDetailMap = convertMap(propertyValueDetails, ProductPropertyValueDetailRespDTO::getValueId);
        for (int i = 0; i < orderItems.size(); i++) {
            List<TradeOrderItemDO.Property> properties = orderItems.get(i).getProperties();
            if (CollUtil.isEmpty(properties)) {
                continue;
            }
            AppTradeOrderItemRespVO item = orderVO.getItems().get(i);
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
        if (express != null) {
            orderVO.setLogisticsId(express.getId()).setLogisticsName(express.getName());
        }
        return orderVO;
    }

    AppTradeOrderDetailRespVO convert3(TradeOrderDO order, List<TradeOrderItemDO> items);

    AppTradeOrderItemRespVO convert03(TradeOrderItemDO bean);

    @Mappings({
            @Mapping(target = "skuId", source = "tradeOrderItemDO.skuId"),
            @Mapping(target = "orderId", source = "tradeOrderItemDO.orderId"),
            @Mapping(target = "orderItemId", source = "tradeOrderItemDO.id"),
            @Mapping(target = "descriptionScores", source = "createReqVO.descriptionScores"),
            @Mapping(target = "benefitScores", source = "createReqVO.benefitScores"),
            @Mapping(target = "content", source = "createReqVO.content"),
            @Mapping(target = "picUrls", source = "createReqVO.picUrls"),
            @Mapping(target = "anonymous", source = "createReqVO.anonymous"),
            @Mapping(target = "userId", source = "tradeOrderItemDO.userId")
    })
    ProductCommentCreateReqDTO convert04(AppTradeOrderItemCommentCreateReqVO createReqVO, TradeOrderItemDO tradeOrderItemDO);

    default TradePriceCalculateReqBO convert(Long userId, AppTradeOrderSettlementReqVO settlementReqVO,
                                             List<TradeCartDO> cartList) {
        TradePriceCalculateReqBO reqBO = new TradePriceCalculateReqBO();
        reqBO.setUserId(userId).setCouponId(settlementReqVO.getCouponId()).setAddressId(settlementReqVO.getAddressId())
                .setItems(new ArrayList<>(settlementReqVO.getItems().size()));
        // 商品项的构建
        Map<Long, TradeCartDO> cartMap = convertMap(cartList, TradeCartDO::getId);
        for (AppTradeOrderSettlementReqVO.Item item : settlementReqVO.getItems()) {
            // 情况一：skuId + count
            if (item.getSkuId() != null) {
                reqBO.getItems().add(new TradePriceCalculateReqBO.Item().setSkuId(item.getSkuId()).setCount(item.getCount())
                        .setSelected(true)); // true 的原因，下单一定选中
                continue;
            }
            // 情况二：cartId
            TradeCartDO cart = cartMap.get(item.getCartId());
            if (cart == null) {
                continue;
            }
            reqBO.getItems().add(new TradePriceCalculateReqBO.Item().setSkuId(cart.getSkuId()).setCount(cart.getCount())
                    .setCartId(item.getCartId()).setSelected(true)); // true 的原因，下单一定选中
        }
        return reqBO;
    }

    default AppTradeOrderSettlementRespVO convert(TradePriceCalculateRespBO calculate, AddressRespDTO address) {
        AppTradeOrderSettlementRespVO respVO = convert0(calculate, address);
        if (address != null) {
            respVO.getAddress().setAreaName(AreaUtils.format(address.getAreaId()));
        }
        // TODO 芋艿：积分的接入；
        respVO.setUsedPoint(1);
        respVO.setTotalPoint(100);
        return respVO;
    }

    AppTradeOrderSettlementRespVO convert0(TradePriceCalculateRespBO calculate, AddressRespDTO address);

    @Mappings({
            @Mapping(target = "activityId", source = "createReqVO.combinationActivityId"),
            @Mapping(target = "spuId", source = "orderItem.spuId"),
            @Mapping(target = "skuId", source = "orderItem.skuId"),
            @Mapping(target = "userId", source = "order.userId"),
            @Mapping(target = "orderId", source = "order.id"),
            @Mapping(target = "headId", source = "createReqVO.combinationHeadId"),
            @Mapping(target = "spuName", source = "orderItem.spuName"),
            @Mapping(target = "picUrl", source = "orderItem.picUrl"),
            @Mapping(target = "combinationPrice", source = "orderItem.payPrice"),
            @Mapping(target = "nickname", source = "user.nickname"),
            @Mapping(target = "avatar", source = "user.avatar"),
            @Mapping(target = "status", ignore = true)
    })
    CombinationRecordCreateReqDTO convert(TradeOrderDO order, TradeOrderItemDO orderItem,
                                          AppTradeOrderCreateReqVO createReqVO, MemberUserRespDTO user);

}
