package cn.iocoder.yudao.module.trade.service.aftersale;

import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.AppAfterSaleCreateReqVO;

/**
 * 交易售后 Service 接口
 *
 * @author 芋道源码
 */
public interface TradeAfterSaleService {

    /**
     * 创建交易售后
     * <p>
     * 一般是用户发起售后请求
     *
     * @param userId 用户编号
     * @param createReqVO 交易售后 Request 信息
     * @return 交易售后编号
     */
    Long createAfterSale(Long userId, AppAfterSaleCreateReqVO createReqVO);

}
