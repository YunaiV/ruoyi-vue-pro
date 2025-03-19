package cn.iocoder.yudao.module.trade.service.price;

import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeProductSettlementRespVO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 价格计算 Service 接口
 *
 * @author 芋道源码
 */
public interface TradePriceService {

    /**
     * 【订单】价格计算
     *
     * @param calculateReqDTO 计算信息
     * @return 计算结果
     */
    TradePriceCalculateRespBO calculateOrderPrice(@Valid TradePriceCalculateReqBO calculateReqDTO);

    /**
     * 【商品】价格计算，用于商品列表、商品详情
     *
     * @param userId 用户编号，允许为空
     * @param spuIds 商品 SPU 编号数组
     * @return 计算结果
     */
    List<AppTradeProductSettlementRespVO> calculateProductPrice(Long userId, List<Long> spuIds);

}
