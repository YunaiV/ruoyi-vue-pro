package cn.iocoder.yudao.module.trade.controller.app.order;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.member.api.level.MemberLevelApi;
import cn.iocoder.yudao.module.member.api.level.dto.MemberLevelRespDTO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.promotion.api.discount.DiscountActivityApi;
import cn.iocoder.yudao.module.promotion.api.discount.dto.DiscountProductRespDTO;
import cn.iocoder.yudao.module.promotion.api.reward.RewardActivityApi;
import cn.iocoder.yudao.module.promotion.api.reward.dto.RewardActivityMatchRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionConditionTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionDiscountTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.*;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.item.AppTradeOrderItemCommentCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.item.AppTradeOrderItemRespVO;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.aftersale.AfterSaleService;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderQueryService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderUpdateService;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.DISCOUNT_ACTIVITY_TYPE_NOT_EXISTS;

@Tag(name = "用户 App - 交易订单")
@RestController
@RequestMapping("/trade/order")
@Validated
@Slf4j
public class AppTradeOrderController {

    @Resource
    private TradeOrderUpdateService tradeOrderUpdateService;
    @Resource
    private TradeOrderQueryService tradeOrderQueryService;
    @Resource
    private DeliveryExpressService deliveryExpressService;

    @Resource
    private AfterSaleService afterSaleService;

    @Resource
    private TradeOrderProperties tradeOrderProperties;

    @Resource
    private MemberLevelApi memberLevelApi;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private DiscountActivityApi discountActivityApi;
    @Resource
    private RewardActivityApi rewardActivityApi;
    @Resource
    private ProductSkuApi productKpuApi;

    @GetMapping("/settlement")
    @Operation(summary = "获得订单结算信息")
    @PreAuthenticated
    public CommonResult<AppTradeOrderSettlementRespVO> settlementOrder(@Valid AppTradeOrderSettlementReqVO settlementReqVO) {
        return success(tradeOrderUpdateService.settlementOrder(getLoginUserId(), settlementReqVO));
    }

    @GetMapping("/settlementProduct")
    @Operation(summary = "获得商品结算信息")
    public CommonResult<List<AppTradeProductSettlementRespVO>> settlementProduct(@RequestParam("ids") Set<Long> ids) {
        List<AppTradeProductSettlementRespVO> appTradeProductSettlementRespVOS = new ArrayList<>();
        MemberLevelRespDTO memberLevel = getMemberLevel();
        ids.forEach(spuId -> {
            List<AppTradeProductSettlementRespVO.Sku> skus = new ArrayList<>();
            List<ProductSkuRespDTO> skuList = productKpuApi.getSkuListBySpuId(Collections.singletonList(spuId));
            //查询sku的会员和限时优惠
            skuList.forEach(sku -> {
                //查询限时优惠价格
                AppTradeProductSettlementRespVO.Sku skuDiscount = calculateDiscountPrice(sku.getId(), sku.getPrice());

                //查询会员价
                AppTradeProductSettlementRespVO.Sku skuVip = calculateVipPrice(sku.getId(), sku.getPrice(), memberLevel);

                if(skuDiscount != null && skuVip != null){
                    if(skuDiscount.getPrice() > skuVip.getPrice()){
                        skus.add(skuDiscount);
                    }else{
                        skus.add(skuVip);
                    }
                }else if(skuDiscount != null){
                    skus.add(skuDiscount);
                }else if(skuVip != null){
                    skus.add(skuVip);
                }

            });
            AppTradeProductSettlementRespVO.Reward reward = calculateReward(spuId);
            AppTradeProductSettlementRespVO respVO = AppTradeProductSettlementRespVO.builder().id(spuId).skus(skus).build();
            if(reward != null){
                //创建满减活动对象
                respVO.setReward(reward);
            }
            appTradeProductSettlementRespVOS.add(respVO);
        });
        return success(appTradeProductSettlementRespVOS);
    }

    private MemberLevelRespDTO getMemberLevel() {
        Long userId = getLoginUserId();
        if (userId == null) {
            return null;
        }
        MemberUserRespDTO user = memberUserApi.getUser(userId);
        if (user.getLevelId() == null || user.getLevelId() <= 0) {
            return null;
        }
        return memberLevelApi.getMemberLevel(user.getLevelId());
    }

    @PostMapping("/create")
    @Operation(summary = "创建订单")
    @PreAuthenticated
    public CommonResult<AppTradeOrderCreateRespVO> createOrder(@Valid @RequestBody AppTradeOrderCreateReqVO createReqVO) {
        TradeOrderDO order = tradeOrderUpdateService.createOrder(getLoginUserId(), createReqVO);
        return success(new AppTradeOrderCreateRespVO().setId(order.getId()).setPayOrderId(order.getPayOrderId()));
    }

    @PostMapping("/update-paid")
    @Operation(summary = "更新订单为已支付") // 由 pay-module 支付服务，进行回调，可见 PayNotifyJob
    public CommonResult<Boolean> updateOrderPaid(@RequestBody PayOrderNotifyReqDTO notifyReqDTO) {
        tradeOrderUpdateService.updateOrderPaid(Long.valueOf(notifyReqDTO.getMerchantOrderId()),
                notifyReqDTO.getPayOrderId());
        return success(true);
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得交易订单")
    @Parameter(name = "id", description = "交易订单编号")
    @PreAuthenticated
    public CommonResult<AppTradeOrderDetailRespVO> getOrder(@RequestParam("id") Long id) {
        // 查询订单
        TradeOrderDO order = tradeOrderQueryService.getOrder(getLoginUserId(), id);
        if (order == null) {
            return success(null);
        }

        // 查询订单项
        List<TradeOrderItemDO> orderItems = tradeOrderQueryService.getOrderItemListByOrderId(order.getId());
        // 查询物流公司
        DeliveryExpressDO express = order.getLogisticsId() != null && order.getLogisticsId() > 0 ?
                deliveryExpressService.getDeliveryExpress(order.getLogisticsId()) : null;
        // 最终组合
        return success(TradeOrderConvert.INSTANCE.convert02(order, orderItems, tradeOrderProperties, express));
    }

    @GetMapping("/get-express-track-list")
    @Operation(summary = "获得交易订单的物流轨迹")
    @Parameter(name = "id", description = "交易订单编号")
    @PreAuthenticated
    public CommonResult<List<AppOrderExpressTrackRespDTO>> getOrderExpressTrackList(@RequestParam("id") Long id) {
        return success(TradeOrderConvert.INSTANCE.convertList02(
                tradeOrderQueryService.getExpressTrackList(id, getLoginUserId())));
    }

    @GetMapping("/page")
    @Operation(summary = "获得交易订单分页")
    @PreAuthenticated
    public CommonResult<PageResult<AppTradeOrderPageItemRespVO>> getOrderPage(AppTradeOrderPageReqVO reqVO) {
        // 查询订单
        PageResult<TradeOrderDO> pageResult = tradeOrderQueryService.getOrderPage(getLoginUserId(), reqVO);
        // 查询订单项
        List<TradeOrderItemDO> orderItems = tradeOrderQueryService.getOrderItemListByOrderId(
                convertSet(pageResult.getList(), TradeOrderDO::getId));
        // 最终组合
        return success(TradeOrderConvert.INSTANCE.convertPage02(pageResult, orderItems));
    }

    @GetMapping("/get-count")
    @Operation(summary = "获得交易订单数量")
    @PreAuthenticated
    public CommonResult<Map<String, Long>> getOrderCount() {
        Map<String, Long> orderCount = Maps.newLinkedHashMapWithExpectedSize(5);
        // 全部
        orderCount.put("allCount", tradeOrderQueryService.getOrderCount(getLoginUserId(), null, null));
        // 待付款（未支付）
        orderCount.put("unpaidCount", tradeOrderQueryService.getOrderCount(getLoginUserId(),
                TradeOrderStatusEnum.UNPAID.getStatus(), null));
        // 待发货
        orderCount.put("undeliveredCount", tradeOrderQueryService.getOrderCount(getLoginUserId(),
                TradeOrderStatusEnum.UNDELIVERED.getStatus(), null));
        // 待收货
        orderCount.put("deliveredCount", tradeOrderQueryService.getOrderCount(getLoginUserId(),
                TradeOrderStatusEnum.DELIVERED.getStatus(), null));
        // 待评价
        orderCount.put("uncommentedCount", tradeOrderQueryService.getOrderCount(getLoginUserId(),
                TradeOrderStatusEnum.COMPLETED.getStatus(), false));
        // 售后数量
        orderCount.put("afterSaleCount", afterSaleService.getApplyingAfterSaleCount(getLoginUserId()));
        return success(orderCount);
    }

    @PutMapping("/receive")
    @Operation(summary = "确认交易订单收货")
    @Parameter(name = "id", description = "交易订单编号")
    @PreAuthenticated
    public CommonResult<Boolean> receiveOrder(@RequestParam("id") Long id) {
        tradeOrderUpdateService.receiveOrderByMember(getLoginUserId(), id);
        return success(true);
    }

    @DeleteMapping("/cancel")
    @Operation(summary = "取消交易订单")
    @Parameter(name = "id", description = "交易订单编号")
    @PreAuthenticated
    public CommonResult<Boolean> cancelOrder(@RequestParam("id") Long id) {
        tradeOrderUpdateService.cancelOrderByMember(getLoginUserId(), id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除交易订单")
    @Parameter(name = "id", description = "交易订单编号")
    @PreAuthenticated
    public CommonResult<Boolean> deleteOrder(@RequestParam("id") Long id) {
        tradeOrderUpdateService.deleteOrder(getLoginUserId(), id);
        return success(true);
    }

    // ========== 订单项 ==========

    @GetMapping("/item/get")
    @Operation(summary = "获得交易订单项")
    @Parameter(name = "id", description = "交易订单项编号")
    @PreAuthenticated
    public CommonResult<AppTradeOrderItemRespVO> getOrderItem(@RequestParam("id") Long id) {
        TradeOrderItemDO item = tradeOrderQueryService.getOrderItem(getLoginUserId(), id);
        return success(TradeOrderConvert.INSTANCE.convert03(item));
    }

    @PostMapping("/item/create-comment")
    @Operation(summary = "创建交易订单项的评价")
    @PreAuthenticated
    public CommonResult<Long> createOrderItemComment(@RequestBody AppTradeOrderItemCommentCreateReqVO createReqVO) {
        return success(tradeOrderUpdateService.createOrderItemCommentByMember(getLoginUserId(), createReqVO));
    }

    /**
     * 计算会员 VIP 优惠价格
     *
     * @param price 原价
     * @param memberLevel 会员等级
     * @return 优惠价格
     */
    public AppTradeProductSettlementRespVO.Sku calculateVipPrice(Long skuId, Integer price, MemberLevelRespDTO memberLevel) {
        if (memberLevel == null || memberLevel.getDiscountPercent() == null) {
            return null;
        }
        Integer newPrice = price * memberLevel.getDiscountPercent() / 100;
        return AppTradeProductSettlementRespVO.Sku.builder().
                skuId(skuId).
                type(PromotionTypeEnum.MEMBER_LEVEL.getType()).
                price(price - newPrice).build();
    }

    /**
     * 计算限时优惠信息
     *
     * @param price 原价
     * @param skuId 商品规格id
     * @return 优惠价格
     */
    private AppTradeProductSettlementRespVO.Sku calculateDiscountPrice(Long skuId, Integer price) {
        if (skuId == null) {
            return null;
        }

        //根据商品id查询限时优惠
        List<DiscountProductRespDTO> matchDiscountProductList = discountActivityApi.getMatchDiscountProductList(Collections.singletonList(skuId));
        if (matchDiscountProductList != null && !matchDiscountProductList.isEmpty()) {
            DiscountProductRespDTO discountProductRespDTO = matchDiscountProductList.get(matchDiscountProductList.size() - 1);
            AppTradeProductSettlementRespVO.Sku sku = AppTradeProductSettlementRespVO.Sku.builder().
                    skuId(skuId).
                    discountId(discountProductRespDTO.getId()).
                    type(PromotionTypeEnum.DISCOUNT_ACTIVITY.getType()).
                    endTime(discountProductRespDTO.getActivityEndTime()).
                    build();
            Integer discountType = discountProductRespDTO.getDiscountType();
            if(Objects.equals(PromotionDiscountTypeEnum.PRICE.getType(), discountType)){
                sku.setPrice(price - discountProductRespDTO.getDiscountPrice() * 100);
            }else if(Objects.equals(PromotionDiscountTypeEnum.PERCENT.getType(), discountType)){
                Integer newPrice = price * discountProductRespDTO.getDiscountPercent() / 100;
                sku.setPrice(price - newPrice);
            }else{
                throw exception(DISCOUNT_ACTIVITY_TYPE_NOT_EXISTS);
            }
            return sku;
        }
        return null;
    }

    /**
     * 获取第一层满减活动
     *
     * @param spuId 商品规格id
     * @return 优惠价格
     */
    private AppTradeProductSettlementRespVO.Reward calculateReward(Long spuId) {
        List<RewardActivityMatchRespDTO> matchRewardActivityList = rewardActivityApi.getRewardActivityBySpuIdsAndStatusAndDateTimeLt(Collections.singletonList(spuId), CommonStatusEnum.ENABLE.getStatus(), LocalDateTime.now());
        if(matchRewardActivityList != null && !matchRewardActivityList.isEmpty()){
            RewardActivityMatchRespDTO rewardActivityMatchRespDTO = matchRewardActivityList.get(matchRewardActivityList.size() - 1);
            if(rewardActivityMatchRespDTO != null){
                RewardActivityMatchRespDTO.Rule rule = rewardActivityMatchRespDTO.getRules().get(0);
                return AppTradeProductSettlementRespVO.Reward.builder().
                        rewardActivity("满" + rule.getLimit() / 100 + (Objects.equals(rewardActivityMatchRespDTO.getConditionType(), PromotionConditionTypeEnum.PRICE.getType())?"元":"件"+"减") +rule.getDiscountPrice() / 100)
                        .rewardId(rewardActivityMatchRespDTO.getId()).build();
            }
        }
        return null;
    }

}
