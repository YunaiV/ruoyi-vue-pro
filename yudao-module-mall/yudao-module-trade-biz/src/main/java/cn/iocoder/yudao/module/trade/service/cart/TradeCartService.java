package cn.iocoder.yudao.module.trade.service.cart;

import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartListRespVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartAddReqVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartUpdateReqVO;

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
     * @param addReqVO 添加信息
     * @return 购物项的编号
     */
    Long addCart(Long userId, @Valid AppTradeCartAddReqVO addReqVO);

    /**
     * 更新购物车商品数量
     *
     * @param userId 用户编号
     * @param updateCountReqVO 更新信息
     */
    void updateCart(Long userId, AppTradeCartUpdateReqVO updateCountReqVO);

    /**
     * 删除购物车商品
     *
     * @param userId 用户编号
     * @param ids 购物项的编号
     */
    void deleteCart(Long userId, Collection<Long> ids);

    /**
     * 查询用户在购物车中的商品数量
     *
     * @param userId 用户编号
     * @return 商品数量
     */
    Integer getCartCount(Long userId);

    /**
     * 查询用户的购物车列表
     *
     * @param userId 用户编号
     * @return 购物车列表
     */
    AppTradeCartListRespVO getCartList(Long userId);

}
