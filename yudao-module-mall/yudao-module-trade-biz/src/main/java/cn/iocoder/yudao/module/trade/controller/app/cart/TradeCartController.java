package cn.iocoder.yudao.module.trade.controller.app.cart;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartItemAddCountReqVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartItemUpdateCountReqVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartItemUpdateSelectedReqVO;
import cn.iocoder.yudao.module.trade.service.cart.TradeCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Api(tags = "用户 App - 购物车")
@RestController
@RequestMapping("/trade/cart")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TradeCartController {

    @Resource
    private TradeCartService cartService;

    @PostMapping("/add-count")
    @ApiOperation("添加商品到购物车")
    @PreAuthenticated
    public CommonResult<Boolean> addCartItemCount(@Valid @RequestBody AppTradeCartItemAddCountReqVO addCountReqVO) {
        cartService.addCartItemCount(getLoginUserId(), addCountReqVO);
        return success(true);
    }

    @PutMapping("update-count")
    @ApiOperation("更新购物车商品数量")
    @PreAuthenticated
    public CommonResult<Boolean> updateCartItemQuantity(@Valid @RequestBody AppTradeCartItemUpdateCountReqVO updateCountReqVO) {
        cartService.updateCartItemCount(getLoginUserId(), updateCountReqVO);
        return success(true);
    }

    @PutMapping("update-selected")
    @ApiOperation("更新购物车商品是否选中")
    @PreAuthenticated
    public CommonResult<Boolean> updateCartItemSelected(@Valid @RequestBody AppTradeCartItemUpdateSelectedReqVO updateSelectedReqVO) {
        cartService.updateCartItemSelected(getLoginUserId(), updateSelectedReqVO);
        // 获得目前购物车明细
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除购物车商品")
    @ApiImplicitParam(name = "skuIds", value = "商品 SKU 编号的数组", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthenticated
    public CommonResult<Boolean> deleteCartItem(@RequestParam("skuIds") List<Long> skuIds) {
        cartService.deleteCartItems(getLoginUserId(), skuIds);
        return success(true);
    }

    @GetMapping("get-count")
    @ApiOperation("查询用户在购物车中的商品数量")
    @PreAuthenticated
    public CommonResult<Integer> getCartCount() {
        return success(cartService.getCartCount(getLoginUserId()));
    }

    @GetMapping("/get-detail")
    @ApiOperation("查询用户的购物车的详情")
    @PreAuthenticated
    public CommonResult<AppTradeCartDetailRespVO> getCartDetail() {
        return success(cartService.getCartDetail(getLoginUserId()));
    }

}
