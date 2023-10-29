package cn.iocoder.yudao.module.trade.convert.order;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.framework.dict.core.util.DictFrameworkUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.member.api.address.dto.MemberAddressRespDTO;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.enums.DictTypeConstants;
import cn.iocoder.yudao.module.product.api.comment.dto.ProductCommentCreateReqDTO;
import cn.iocoder.yudao.module.product.api.property.dto.ProductPropertyValueDetailRespDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateStockReqDTO;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.trade.api.order.dto.TradeOrderRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.base.member.user.MemberUserRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.base.product.property.ProductPropertyValueDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.trade.controller.app.base.property.AppProductPropertyValueDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.*;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.item.AppTradeOrderItemCommentCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.item.AppTradeOrderItemRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.CartDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderLogDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackRespDTO;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.BrokerageAddReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.addTime;

@Mapper
public interface TradeOrderConvert {

    TradeOrderConvert INSTANCE = Mappers.getMapper(TradeOrderConvert.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(source = "userId", target = "userId"),
            @Mapping(source = "createReqVO.couponId", target = "couponId"),
            @Mapping(target = "remark", ignore = true),
            @Mapping(source = "createReqVO.remark", target = "userRemark"),
            @Mapping(source = "calculateRespBO.price.totalPrice", target = "totalPrice"),
            @Mapping(source = "calculateRespBO.price.discountPrice", target = "discountPrice"),
            @Mapping(source = "calculateRespBO.price.deliveryPrice", target = "deliveryPrice"),
            @Mapping(source = "calculateRespBO.price.couponPrice", target = "couponPrice"),
            @Mapping(source = "calculateRespBO.price.pointPrice", target = "pointPrice"),
            @Mapping(source = "calculateRespBO.price.vipPrice", target = "vipPrice"),
            @Mapping(source = "calculateRespBO.price.payPrice", target = "payPrice")
    })
    TradeOrderDO convert(Long userId, String userIp, AppTradeOrderCreateReqVO createReqVO,
                         TradePriceCalculateRespBO calculateRespBO);

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

    default ProductSkuUpdateStockReqDTO convert(List<TradeOrderItemDO> list) {
        List<ProductSkuUpdateStockReqDTO.Item> items = CollectionUtils.convertList(list, item ->
                new ProductSkuUpdateStockReqDTO.Item().setId(item.getSkuId()).setIncrCount(item.getCount()));
        return new ProductSkuUpdateStockReqDTO(items);
    }

    default ProductSkuUpdateStockReqDTO convertNegative(List<TradeOrderItemDO> list) {
        List<ProductSkuUpdateStockReqDTO.Item> items = CollectionUtils.convertList(list, item ->
                new ProductSkuUpdateStockReqDTO.Item().setId(item.getSkuId()).setIncrCount(-item.getCount()));
        return new ProductSkuUpdateStockReqDTO(items);
    }

    default PayOrderCreateReqDTO convert(TradeOrderDO order, List<TradeOrderItemDO> orderItems,
                                         TradeOrderProperties orderProperties) {
        PayOrderCreateReqDTO createReqDTO = new PayOrderCreateReqDTO()
                .setAppId(orderProperties.getAppId()).setUserIp(order.getUserIp());
        // 商户相关字段
        createReqDTO.setMerchantOrderId(String.valueOf(order.getId()));
        String subject = orderItems.get(0).getSpuName();
        subject = StrUtils.maxLength(subject, PayOrderCreateReqDTO.SUBJECT_MAX_LENGTH); // 避免超过 32 位
        createReqDTO.setSubject(subject);
        createReqDTO.setBody(subject); // TODO 芋艿：临时写死
        // 订单相关字段
        createReqDTO.setPrice(order.getPayPrice()).setExpireTime(addTime(orderProperties.getPayExpireTime()));
        return createReqDTO;
    }

    default PageResult<TradeOrderPageItemRespVO> convertPage(PageResult<TradeOrderDO> pageResult,
                                                             List<TradeOrderItemDO> orderItems,
                                                             Map<Long, MemberUserRespDTO> memberUserMap) {
        Map<Long, List<TradeOrderItemDO>> orderItemMap = convertMultiMap(orderItems, TradeOrderItemDO::getOrderId);
        // 转化 List
        List<TradeOrderPageItemRespVO> orderVOs = CollectionUtils.convertList(pageResult.getList(), order -> {
            List<TradeOrderItemDO> xOrderItems = orderItemMap.get(order.getId());
            TradeOrderPageItemRespVO orderVO = convert(order, xOrderItems);
            // 处理收货地址
            orderVO.setReceiverAreaName(AreaUtils.format(order.getReceiverAreaId()));
            // 增加用户信息
            orderVO.setUser(convertUser(memberUserMap.get(orderVO.getUserId())));
            // 增加推广人信息
            orderVO.setBrokerageUser(convertUser(memberUserMap.get(orderVO.getBrokerageUserId())));
            return orderVO;
        });
        return new PageResult<>(orderVOs, pageResult.getTotal());
    }

    MemberUserRespVO convertUser(MemberUserRespDTO memberUserRespDTO);

    TradeOrderPageItemRespVO convert(TradeOrderDO order, List<TradeOrderItemDO> items);

    ProductPropertyValueDetailRespVO convert(ProductPropertyValueDetailRespDTO bean);

    default TradeOrderDetailRespVO convert(TradeOrderDO order, List<TradeOrderItemDO> orderItems,
                                           List<TradeOrderLogDO> orderLogs,
                                           MemberUserRespDTO user, MemberUserRespDTO brokerageUser) {
        TradeOrderDetailRespVO orderVO = convert2(order, orderItems);
        // 处理收货地址
        orderVO.setReceiverAreaName(AreaUtils.format(order.getReceiverAreaId()));
        // 处理用户信息
        orderVO.setUser(convert(user));
        orderVO.setBrokerageUser(convert(brokerageUser));
        // 处理日志
        orderVO.setLogs(convertList03(orderLogs));
        return orderVO;
    }
    List<TradeOrderDetailRespVO.OrderLog> convertList03(List<TradeOrderLogDO> orderLogs);

    TradeOrderDetailRespVO convert2(TradeOrderDO order, List<TradeOrderItemDO> items);

    MemberUserRespVO convert(MemberUserRespDTO bean);

    default PageResult<AppTradeOrderPageItemRespVO> convertPage02(PageResult<TradeOrderDO> pageResult,
                                                                  List<TradeOrderItemDO> orderItems) {
        Map<Long, List<TradeOrderItemDO>> orderItemMap = convertMultiMap(orderItems, TradeOrderItemDO::getOrderId);
        // 转化 List
        List<AppTradeOrderPageItemRespVO> orderVOs = CollectionUtils.convertList(pageResult.getList(), order -> {
            List<TradeOrderItemDO> xOrderItems = orderItemMap.get(order.getId());
            return convert02(order, xOrderItems);
        });
        return new PageResult<>(orderVOs, pageResult.getTotal());
    }

    AppTradeOrderPageItemRespVO convert02(TradeOrderDO order, List<TradeOrderItemDO> items);

    AppProductPropertyValueDetailRespVO convert02(ProductPropertyValueDetailRespDTO bean);

    default AppTradeOrderDetailRespVO convert02(TradeOrderDO order, List<TradeOrderItemDO> orderItems,
                                                TradeOrderProperties tradeOrderProperties,
                                                DeliveryExpressDO express) {
        AppTradeOrderDetailRespVO orderVO = convert3(order, orderItems);
        orderVO.setPayExpireTime(addTime(tradeOrderProperties.getPayExpireTime()));
        if (StrUtil.isNotEmpty(order.getPayChannelCode())) {
            orderVO.setPayChannelName(DictFrameworkUtils.getDictDataLabel(DictTypeConstants.CHANNEL_CODE, order.getPayChannelCode()));
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

    TradePriceCalculateReqBO convert(AppTradeOrderSettlementReqVO settlementReqVO);

    default TradePriceCalculateReqBO convert(Long userId, AppTradeOrderSettlementReqVO settlementReqVO,
                                             List<CartDO> cartList) {
        TradePriceCalculateReqBO reqBO = new TradePriceCalculateReqBO().setUserId(userId)
                .setItems(new ArrayList<>(settlementReqVO.getItems().size()))
                .setCouponId(settlementReqVO.getCouponId()).setPointStatus(settlementReqVO.getPointStatus())
                // 物流信息
                .setDeliveryType(settlementReqVO.getDeliveryType()).setAddressId(settlementReqVO.getAddressId())
                .setPickUpStoreId(settlementReqVO.getPickUpStoreId())
                // 各种活动
                .setSeckillActivityId(settlementReqVO.getSeckillActivityId())
                .setBargainRecordId(settlementReqVO.getBargainRecordId())
                .setCombinationActivityId(settlementReqVO.getCombinationActivityId())
                .setCombinationHeadId(settlementReqVO.getCombinationHeadId());
        // 商品项的构建
        Map<Long, CartDO> cartMap = convertMap(cartList, CartDO::getId);
        for (AppTradeOrderSettlementReqVO.Item item : settlementReqVO.getItems()) {
            // 情况一：skuId + count
            if (item.getSkuId() != null) {
                reqBO.getItems().add(new TradePriceCalculateReqBO.Item().setSkuId(item.getSkuId()).setCount(item.getCount())
                        .setSelected(true)); // true 的原因，下单一定选中
                continue;
            }
            // 情况二：cartId
            CartDO cart = cartMap.get(item.getCartId());
            if (cart == null) {
                continue;
            }
            reqBO.getItems().add(new TradePriceCalculateReqBO.Item().setSkuId(cart.getSkuId()).setCount(cart.getCount())
                    .setCartId(item.getCartId()).setSelected(true)); // true 的原因，下单一定选中
        }
        return reqBO;
    }

    default AppTradeOrderSettlementRespVO convert(TradePriceCalculateRespBO calculate, MemberAddressRespDTO address) {
        AppTradeOrderSettlementRespVO respVO = convert0(calculate, address);
        if (address != null) {
            respVO.getAddress().setAreaName(AreaUtils.format(address.getAreaId()));
        }
        return respVO;
    }

    AppTradeOrderSettlementRespVO convert0(TradePriceCalculateRespBO calculate, MemberAddressRespDTO address);

    List<AppOrderExpressTrackRespDTO> convertList02(List<ExpressTrackRespDTO> list);

    TradeOrderDO convert(TradeOrderUpdateAddressReqVO reqVO);

    TradeOrderDO convert(TradeOrderUpdatePriceReqVO reqVO);

    TradeOrderDO convert(TradeOrderRemarkReqVO reqVO);

    default BrokerageAddReqBO convert(MemberUserRespDTO user, TradeOrderItemDO item,
                                      ProductSpuRespDTO spu, ProductSkuRespDTO sku) {
        BrokerageAddReqBO bo = new BrokerageAddReqBO().setBizId(String.valueOf(item.getId())).setSourceUserId(item.getUserId())
                .setBasePrice(item.getPayPrice() * item.getCount())
                .setTitle(StrUtil.format("{}成功购买{}", user.getNickname(), item.getSpuName()))
                .setFirstFixedPrice(0).setSecondFixedPrice(0);
        if (BooleanUtil.isTrue(spu.getSubCommissionType())) {
            bo.setFirstFixedPrice(sku.getFirstBrokeragePrice()).setSecondFixedPrice(sku.getSecondBrokeragePrice());
        }
        return bo;
    }

    @Named("convertList04")
    List<TradeOrderRespDTO> convertList04(List<TradeOrderDO> list);

    @Mappings({
            @Mapping(target = "activityId", source = "order.combinationActivityId"),
            @Mapping(target = "spuId", source = "item.spuId"),
            @Mapping(target = "skuId", source = "item.skuId"),
            @Mapping(target = "count", source = "item.count"),
            @Mapping(target = "orderId", source = "order.id"),
            @Mapping(target = "userId", source = "order.userId"),
            @Mapping(target = "headId", source = "order.combinationHeadId"),
            @Mapping(target = "combinationPrice", source = "item.payPrice"),
    })
    CombinationRecordCreateReqDTO convert(TradeOrderDO order, TradeOrderItemDO item);

}
