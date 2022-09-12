package cn.iocoder.yudao.module.trade.service.cart;

import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartItemAddCountReqVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartItemUpdateCountReqVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartItemUpdateSelectedReqVO;

import javax.validation.Valid;
import java.util.Collection;

/**
 * 购物车 Service 接口
 *
 * @author 芋道源码
 */
public interface TradeCartService {

    /**
     * 添加商品到购物车
     *
     * @param userId 用户编号
     * @param addCountReqVO 添加信息
     */
    void addCartItemCount(Long userId, @Valid AppTradeCartItemAddCountReqVO addCountReqVO);

    /**
     * 更新购物车商品数量
     *
     * @param userId 用户编号
     * @param updateCountReqVO 更新信息
     */
    void updateCartItemCount(Long userId, AppTradeCartItemUpdateCountReqVO updateCountReqVO);

    /**
     * 更新购物车商品是否选中
     *
     * @param userId 用户编号
     * @param updateSelectedReqVO 更新信息
     */
    void updateCartItemSelected(Long userId, AppTradeCartItemUpdateSelectedReqVO updateSelectedReqVO);

    /**
     * 删除购物车商品
     *
     * @param userId 用户编号
     * @param skuIds SKU 编号的数组
     */
    void deleteCartItems(Long userId, Collection<Long> skuIds);

    /**
     * 查询用户在购物车中的商品数量
     *
     * @param userId 用户编号
     * @return 商品数量
     */
    Integer getCartCount(Long userId);

    /**
     * 查询用户的购物车详情
     *
     * @param userId 用户编号
     * @return 购物车详情
     */
    AppTradeCartDetailRespVO getCartDetail(Long userId);

}
