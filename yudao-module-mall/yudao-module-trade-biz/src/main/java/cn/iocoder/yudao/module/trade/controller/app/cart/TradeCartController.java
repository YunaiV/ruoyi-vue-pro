package cn.iocoder.yudao.module.trade.controller.app.cart;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartListRespVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartAddReqVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartResetReqVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartUpdateReqVO;
import cn.iocoder.yudao.module.trade.service.cart.TradeCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 购物车")
@RestController
@RequestMapping("/trade/cart")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TradeCartController {

    @Resource
    private TradeCartService cartService;

    @PostMapping("/add")
    @Operation(summary = "添加购物车商品")
    @PreAuthenticated
    public CommonResult<Long> addCart(@Valid @RequestBody AppTradeCartAddReqVO addCountReqVO) {
        return success(cartService.addCart(getLoginUserId(), addCountReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新购物车商品")
    @PreAuthenticated
    public CommonResult<Boolean> updateCart(@Valid @RequestBody AppTradeCartUpdateReqVO updateReqVO) {
        cartService.updateCart(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @PutMapping("/reset")
    @Operation(summary = "重置购物车商品")
    @PreAuthenticated
    public CommonResult<Boolean> resetCart(@Valid @RequestBody AppTradeCartResetReqVO updateReqVO) {
        cartService.resetCart(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除购物车商品")
    @Parameter(name = "ids", description = "购物车商品编号", required = true, example = "1024,2048")
    @PreAuthenticated
    public CommonResult<Boolean> deleteCart(@RequestParam("ids") List<Long> ids) {
        cartService.deleteCart(getLoginUserId(), ids);
        return success(true);
    }

    @GetMapping("get-count")
    @Operation(summary = "查询用户在购物车中的商品数量")
    @PreAuthenticated
    public CommonResult<Integer> getCartCount() {
        return success(cartService.getCartCount(getLoginUserId()));
    }

    @GetMapping("/list")
    @Operation(summary = "查询用户的购物车列表")
    @PreAuthenticated
    public CommonResult<AppTradeCartListRespVO> getCartList() {
        return success(cartService.getCartList(getLoginUserId()));
    }

}
