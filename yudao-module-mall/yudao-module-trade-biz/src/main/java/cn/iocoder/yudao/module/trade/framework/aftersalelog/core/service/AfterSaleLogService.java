package cn.iocoder.yudao.module.trade.framework.aftersalelog.core.service;


import cn.iocoder.yudao.module.trade.framework.aftersalelog.core.dto.TradeAfterSaleLogCreateReqDTO;

/**
 * 交易售后日志 Service 接口
 *
 * @author 陈賝
 * @since 2023/6/12 14:18
 */
public interface AfterSaleLogService {

    /**
     * 创建售后日志
     *
     * @param logDTO 日志记录
     * @author 陈賝
     * @since 2023/6/12 14:18
     */
    void createLog(TradeAfterSaleLogCreateReqDTO logDTO);

}
