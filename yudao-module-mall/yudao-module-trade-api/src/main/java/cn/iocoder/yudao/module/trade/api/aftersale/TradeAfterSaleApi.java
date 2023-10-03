package cn.iocoder.yudao.module.trade.api.aftersale;

import cn.iocoder.yudao.module.trade.api.aftersale.dto.AfterSaleSummaryRespDTO;

import java.time.LocalDateTime;

/**
 * 售后 API 接口
 *
 * @author owen
 */
public interface TradeAfterSaleApi {

    /**
     * 获取售后单统计
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 售后统计结果
     */
    AfterSaleSummaryRespDTO getAfterSaleSummary(LocalDateTime beginTime, LocalDateTime endTime);

}
