package cn.iocoder.yudao.module.statistics.service.trade.bo;

import lombok.Data;

/**
 * 交易统计 Resp BO
 *
 * @author owen
 */
@Data
public class TradeSummaryRespBO {

    /**
     * 数量
     */
    private Integer count;

    /**
     * 合计
     */
    private Integer summary;

}
