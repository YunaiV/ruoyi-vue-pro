package cn.iocoder.yudao.module.erp.service.statistics;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 销售统计 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpSaleStatisticsService {

    /**
     * 获得销售金额
     *
     * 计算逻辑：销售出库的金额 - 销售退货的金额
     *
     * @param beginTime >= 开始时间
     * @param endTime < 结束时间
     * @return 销售金额
     */
    BigDecimal getSalePrice(LocalDateTime beginTime, LocalDateTime endTime);

}
