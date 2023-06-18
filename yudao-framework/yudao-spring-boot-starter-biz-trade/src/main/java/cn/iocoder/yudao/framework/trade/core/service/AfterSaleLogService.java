package cn.iocoder.yudao.framework.trade.core.service;

import cn.iocoder.yudao.framework.trade.core.dto.TradeAfterSaleLogCreateReqDTO;

// TODO @陈賝：类注释
public interface AfterSaleLogService {

    /**
     * 创建售后日志
     *
     * @param logDTO 日志记录
     * @author 陈賝
     * @since 2023/6/12 14:18
     */
    // TODO @陈賝：createLog 方法名
    void insert(TradeAfterSaleLogCreateReqDTO logDTO);

}
