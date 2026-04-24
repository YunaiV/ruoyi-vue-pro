package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import cn.iocoder.yudao.module.deepay.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Deepay 支付接口。
 *
 * <ul>
 *   <li>POST /api/payment/order/create —— 创建订单（INIT）</li>
 *   <li>POST /api/payment/callback —— Jeepay Webhook 回调（INIT → PAID）</li>
 *   <li>POST /api/payment/simulate —— 模拟支付成功（测试用）</li>
 *   <li>GET  /product/{chainCode}  —— 商品详情页</li>
 * </ul>
 */
@Tag(name = "Deepay - 支付收款")
@RestController
@RequestMapping
@Validated
public class DeepayPaymentController {

    @Resource
    private OrderService orderService;

    // ----------------------------------------------------------------
    // 创建订单
    // ----------------------------------------------------------------

    @PostMapping("/api/payment/order/create")
    @Operation(summary = "创建订单（INIT）")
    public CommonResult<Map<String, Object>> createOrder(@Valid @RequestBody ChainCodeReqVO req) {
        DeepayOrderDO order = orderService.createOrder(req.getChainCode());
        return success(orderToMap(order));
    }

    // ----------------------------------------------------------------
    // Jeepay Webhook 回调（生产环境由支付网关调用）
    // ----------------------------------------------------------------

    @PostMapping("/api/payment/callback")
    @Operation(summary = "Jeepay / Swan 支付回调（Webhook）")
    public CommonResult<Map<String, Object>> paymentCallback(@Valid @RequestBody ChainCodeReqVO req) {
        DeepayOrderDO order = orderService.markPaid(req.getChainCode());
        return success(orderToMap(order));
    }

    // ----------------------------------------------------------------
    // 模拟支付成功（MVP 测试专用）
    // ----------------------------------------------------------------

    @PostMapping("/api/payment/simulate")
    @Operation(summary = "模拟支付成功（测试用）")
    public CommonResult<Map<String, Object>> simulatePaid(@Valid @RequestBody ChainCodeReqVO req) {
        DeepayOrderDO order = orderService.markPaid(req.getChainCode());
        return success(orderToMap(order));
    }

    // ----------------------------------------------------------------
    // 商品详情页
    // ----------------------------------------------------------------

    @GetMapping("/product/{chainCode}")
    @Operation(summary = "商品详情页")
    public CommonResult<Map<String, Object>> productDetail(@PathVariable("chainCode") String chainCode) {
        DeepayProductDO product = orderService.getProduct(chainCode);
        Map<String, Object> resp = new LinkedHashMap<>();
        if (product == null) {
            resp.put("chainCode", chainCode);
            resp.put("exists", false);
            return success(resp);
        }
        resp.put("chainCode", product.getChainCode());
        resp.put("title", product.getTitle());
        resp.put("description", product.getDescription());
        resp.put("image", product.getCoverImage());
        resp.put("price", product.getPrice());
        resp.put("status", product.getStatus());
        resp.put("exists", true);
        return success(resp);
    }

    // ----------------------------------------------------------------
    // 内部 VO
    // ----------------------------------------------------------------

    private static Map<String, Object> orderToMap(DeepayOrderDO order) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (order == null) {
            map.put("error", "order not found");
            return map;
        }
        map.put("orderId", order.getId());
        map.put("chainCode", order.getChainCode());
        map.put("amount", order.getAmount());
        map.put("status", order.getStatus());
        return map;
    }

    public static class ChainCodeReqVO {

        @NotBlank(message = "chainCode 不能为空")
        private String chainCode;

        public String getChainCode() {
            return chainCode;
        }

        public void setChainCode(String chainCode) {
            this.chainCode = chainCode;
        }
    }

}
