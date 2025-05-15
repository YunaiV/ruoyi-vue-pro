//package com.somle.esb.job.oms.order;
//
//import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
//import com.somle.esb.converter.oms.ShopifyToOmsConverter;
//import com.somle.shopify.service.ShopifyService;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.stream.Stream;
//
//@Slf4j
//@Component
//public class ShopifyOrdersSyncJob extends BaseOrdersSyncJob {
//    @Resource
//    ShopifyService shopifyService;
//
//    @Resource
//    ShopifyToOmsConverter shopifyToOmsConverter;
//
//    @Override
//    public List<OmsOrderSaveReqDTO> listOrders(String param) {
//        return shopifyService.shopifyClients.stream()
//            .flatMap(client -> {
//                try {
//                    // 假设 getOrders() 返回 List<Order>，toOrders() 转换为 List<OmsOrderSaveReqDTO>
//                    return shopifyToOmsConverter.toOrders(client.getOrders(), client).stream();
//                } catch (Exception e) {
//                    log.error("Failed to fetch orders for client", e);
//                    return Stream.empty(); // 发生异常时返回空流
//                }
//            })
//            .toList();
//    }
//}
