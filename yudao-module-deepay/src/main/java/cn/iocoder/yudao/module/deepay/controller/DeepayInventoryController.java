package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayInventoryDO;
import cn.iocoder.yudao.module.deepay.service.inventory.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Deepay 库存管理接口。
 *
 * <p>提供后台管理员对库存的查看与手动补货功能，以及模拟下单/支付的接口。</p>
 *
 * <ul>
 *   <li>GET  /deepay/inventory/list       —— 查看所有库存</li>
 *   <li>POST /deepay/inventory/add-stock  —— 手动补货</li>
 *   <li>POST /deepay/order/place          —— 模拟下单（锁定库存）</li>
 *   <li>POST /deepay/order/pay            —— 模拟支付成功（释放锁定库存）</li>
 * </ul>
 */
@Tag(name = "Deepay - 库存管理")
@RestController
@RequestMapping("/deepay")
@Validated
public class DeepayInventoryController {

    @Resource
    private InventoryService inventoryService;

    // ---------------------------------------------------------------- inventory

    @GetMapping("/inventory/list")
    @Operation(summary = "查看所有库存")
    public CommonResult<List<DeepayInventoryDO>> listInventory() {
        return success(inventoryService.listAll());
    }

    @PostMapping("/inventory/add-stock")
    @Operation(summary = "手动补充库存")
    public CommonResult<Void> addStock(@Valid @RequestBody AddStockReqVO req) {
        inventoryService.addStock(req.getChainCode(), req.getQuantity());
        return success(null);
    }

    // ------------------------------------------------------------------ order

    @PostMapping("/order/place")
    @Operation(summary = "模拟下单（锁定库存）")
    public CommonResult<Boolean> placeOrder(@Valid @RequestBody OrderReqVO req) {
        boolean success = inventoryService.lockStock(req.getChainCode());
        return success(success);
    }

    @PostMapping("/order/pay")
    @Operation(summary = "模拟支付成功（释放锁定库存）")
    public CommonResult<Void> payOrder(@Valid @RequestBody OrderReqVO req) {
        inventoryService.confirmPayment(req.getChainCode());
        return success(null);
    }

    // ----------------------------------------------------------- 内部请求 VO

    /** 补货请求体 */
    public static class AddStockReqVO {

        @NotBlank(message = "chainCode 不能为空")
        private String chainCode;

        @Min(value = 1, message = "补货数量至少为 1")
        private int quantity;

        public String getChainCode() { return chainCode; }
        public void setChainCode(String chainCode) { this.chainCode = chainCode; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    /** 订单请求体 */
    public static class OrderReqVO {

        @NotBlank(message = "chainCode 不能为空")
        private String chainCode;

        public String getChainCode() { return chainCode; }
        public void setChainCode(String chainCode) { this.chainCode = chainCode; }
    }

}
