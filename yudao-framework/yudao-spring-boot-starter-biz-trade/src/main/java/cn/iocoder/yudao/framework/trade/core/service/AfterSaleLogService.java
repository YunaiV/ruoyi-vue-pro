package cn.iocoder.yudao.framework.trade.core.service;

import cn.iocoder.yudao.framework.trade.core.dto.TradeAfterSaleLogDTO;
import org.springframework.scheduling.annotation.Async;

public interface AfterSaleLogService {

    /**
     * 日志记录
     *
     * @param logDTO 日志记录
     * @author 陈賝
     * @date 2023/6/12 14:18
     */
    void insert(TradeAfterSaleLogDTO logDTO);
}
