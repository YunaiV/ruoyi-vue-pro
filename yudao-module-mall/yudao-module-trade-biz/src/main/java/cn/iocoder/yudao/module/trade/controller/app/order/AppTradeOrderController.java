package cn.iocoder.yudao.module.trade.controller.app.order;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import cn.iocoder.yudao.module.product.api.property.ProductPropertyValueApi;
import cn.iocoder.yudao.module.product.api.property.dto.ProductPropertyValueDetailRespDTO;
import cn.iocoder.yudao.module.trade.controller.app.base.property.AppProductPropertyValueDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.*;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.item.AppTradeOrderItemRespVO;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 交易订单")
@RestController
@RequestMapping("/trade/order")
@Validated
@Slf4j
public class AppTradeOrderController {

    @Resource
    private TradeOrderService tradeOrderService;

    @Resource
    private ProductPropertyValueApi productPropertyValueApi;

    @Resource
    private TradeOrderProperties tradeOrderProperties;

    @GetMapping("/settlement")
    @Operation(summary = "获得订单结算信息")
    @PreAuthenticated
    public CommonResult<AppTradeOrderSettlementRespVO> settlementOrder(
            @Valid AppTradeOrderSettlementReqVO settlementReqVO) {
//        return success(tradeOrderService.getOrderConfirmCreateInfo(UserSecurityContextHolder.getUserId(), skuId, quantity, couponCardId));
        AppTradeOrderSettlementRespVO settlement = new AppTradeOrderSettlementRespVO();

        AppTradeOrderSettlementRespVO.Price price = new AppTradeOrderSettlementRespVO.Price();
        price.setTotalPrice(1000);
        price.setDeliveryPrice(200);
        price.setCouponPrice(100);
        price.setPointPrice(50);
        price.setPayPrice(950);

        List<AppTradeOrderSettlementRespVO.Item> skus = new ArrayList<>();

        AppTradeOrderSettlementRespVO.Item item1 = new AppTradeOrderSettlementRespVO.Item();
        item1.setCartId(1L);
        item1.setSpuId(2048L);
        item1.setSpuName("Apple iPhone 12");
        item1.setSkuId(1024);
        item1.setPrice(500);
        item1.setPicUrl("https://pro.crmeb.net/uploads/attach/2022/10/12/0c56f9abb80d2775fc1e80dbe4f8826a.jpg");
        item1.setCount(2);
        List<AppProductPropertyValueDetailRespVO> properties1 = new ArrayList<>();
        AppProductPropertyValueDetailRespVO property1 = new AppProductPropertyValueDetailRespVO();
        property1.setPropertyId(1L);
        property1.setPropertyName("尺寸");
        property1.setValueId(2L);
        property1.setValueName("大");
        properties1.add(property1);
        item1.setProperties(properties1);

        AppTradeOrderSettlementRespVO.Item item2 = new AppTradeOrderSettlementRespVO.Item();
        item2.setCartId(2L);
        item2.setSpuId(3072L);
        item2.setSpuName("Samsung Galaxy S21");
        item2.setSkuId(2048);
        item2.setPrice(800);
        item2.setPicUrl("https://pro.crmeb.net/uploads/attach/2022/10/12/0c56f9abb80d2775fc1e80dbe4f8826a.jpg");
        item2.setCount(1);
        List<AppProductPropertyValueDetailRespVO> properties2 = new ArrayList<>();
        AppProductPropertyValueDetailRespVO property2 = new AppProductPropertyValueDetailRespVO();
        property2.setPropertyId(10L);
        property2.setPropertyName("颜色");
        property2.setValueId(20L);
        property2.setValueName("白色");
        properties2.add(property2);
        item2.setProperties(properties2);

        skus.add(item1);
        skus.add(item2);

        settlement.setItems(skus);
        settlement.setPrice(price);

        AppTradeOrderSettlementRespVO.Address address = new AppTradeOrderSettlementRespVO.Address();
        address.setId(1L);
        address.setName("John");
        address.setMobile("18888888888");
        address.setProvinceId(1L);
        address.setProvinceName("Beijing");
        address.setCityId(1L);
        address.setCityName("Beijing");
        address.setDistrictId(1L);
        address.setDistrictName("Chaoyang Distripct");
        address.setDetailAddress("No. 10, Xinzhong Street, Chaoyang District");
        address.setDefaulted(true);
        settlement.setAddress(address);

        return success(settlement);
    }

    @PostMapping("/create")
    @Operation(summary = "创建订单")
    @PreAuthenticated
    public CommonResult<Long> createOrder(@RequestBody AppTradeOrderCreateReqVO createReqVO,
                                          HttpServletRequest servletRequest) {
        return success(1L);
//        // 获取登录用户、用户 IP 地址
//        Long loginUserId = getLoginUserId();
//        String clientIp = ServletUtils.getClientIP(servletRequest);
//        // 创建交易订单，预支付记录
//        Long orderId = tradeOrderService.createOrder(loginUserId, clientIp, createReqVO);
//        return success(orderId);
    }

    @PostMapping("/update-paid")
    @Operation(description = "更新订单为已支付") // 由 pay-module 支付服务，进行回调，可见 PayNotifyJob
    public CommonResult<Boolean> updateOrderPaid(@RequestBody PayOrderNotifyReqDTO notifyReqDTO) {
        tradeOrderService.updateOrderPaid(Long.valueOf(notifyReqDTO.getMerchantOrderId()),
                notifyReqDTO.getPayOrderId());
        return success(true);
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得交易订单")
    @Parameter(name = "id", description = "交易订单编号")
    public CommonResult<AppTradeOrderDetailRespVO> getOrder(@RequestParam("id") Long id) {
        // 查询订单
        TradeOrderDO order = tradeOrderService.getOrder(getLoginUserId(), id);
        // 查询订单项
        List<TradeOrderItemDO> orderItems = tradeOrderService.getOrderItemListByOrderId(order.getId());
        // 查询商品属性
        List<ProductPropertyValueDetailRespDTO> propertyValueDetails = productPropertyValueApi
                .getPropertyValueDetailList(TradeOrderConvert.INSTANCE.convertPropertyValueIds(orderItems));
        // 最终组合
        return success(TradeOrderConvert.INSTANCE.convert02(order, orderItems,
                propertyValueDetails, tradeOrderProperties));
    }

    @GetMapping("/page")
    @Operation(summary = "获得交易订单分页")
    public CommonResult<PageResult<AppTradeOrderPageItemRespVO>> getOrderPage(AppTradeOrderPageReqVO reqVO) {
        // 查询订单
        PageResult<TradeOrderDO> pageResult = tradeOrderService.getOrderPage(getLoginUserId(), reqVO);
        // 查询订单项
        List<TradeOrderItemDO> orderItems = tradeOrderService.getOrderItemListByOrderId(
                convertSet(pageResult.getList(), TradeOrderDO::getId));
        // 查询商品属性
        List<ProductPropertyValueDetailRespDTO> propertyValueDetails = productPropertyValueApi
                .getPropertyValueDetailList(TradeOrderConvert.INSTANCE.convertPropertyValueIds(orderItems));
        // 最终组合
        return success(TradeOrderConvert.INSTANCE.convertPage02(pageResult, orderItems, propertyValueDetails));
    }

    @GetMapping("/get-count")
    @Operation(summary = "获得交易订单数量")
    public CommonResult<Map<String, Long>> getOrderCount() {
        Map<String, Long> orderCount = new HashMap<>();
        // 全部
        orderCount.put("allCount", tradeOrderService.getOrderCount(getLoginUserId(), null, null));
        // 待付款（未支付）
        orderCount.put("unpaidCount", tradeOrderService.getOrderCount(getLoginUserId(), TradeOrderStatusEnum.UNPAID.getStatus(), null));
        // 待发货
        orderCount.put("undeliveredCount", tradeOrderService.getOrderCount(getLoginUserId(), TradeOrderStatusEnum.UNDELIVERED.getStatus(), null));
        // 待收货
        orderCount.put("deliveredCount", tradeOrderService.getOrderCount(getLoginUserId(),  TradeOrderStatusEnum.DELIVERED.getStatus(), null));
        // 待评价
        orderCount.put("uncommentedCount", tradeOrderService.getOrderCount(getLoginUserId(), TradeOrderStatusEnum.COMPLETED.getStatus(), false));
        return success(orderCount);
    }

    // ========== 订单项 ==========

    @GetMapping("/item/get")
    @Operation(summary = "获得交易订单项")
    @Parameter(name = "id", description = "交易订单项编号")
    public CommonResult<AppTradeOrderItemRespVO> getOrderItem(@RequestParam("id") Long id) {
        TradeOrderItemDO item = tradeOrderService.getOrderItem(getLoginUserId(), id);
        return success(TradeOrderConvert.INSTANCE.convert03(item));
    }

    // TODO 芋艿：待实现
    @PostMapping("/item/create-comment")
    @Operation(summary = "创建交易订单项的评价")
    public CommonResult<Long> createOrderItemComment() {
        return success(0L);
    }

}
