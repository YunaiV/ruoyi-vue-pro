package cn.iocoder.yudao.module.trade.framework.aftersalelog.core.service;


import cn.iocoder.yudao.module.trade.framework.aftersalelog.core.dto.TradeAfterSaleLogCreateReqDTO;
import cn.iocoder.yudao.module.trade.framework.aftersalelog.core.dto.TradeAfterSaleLogRespDTO;

import java.util.List;

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

    /**
     * 获取售后日志
     *
     * @param afterSaleId 售后编号
     * @return 售后日志
     */
    List<TradeAfterSaleLogRespDTO> getLog(Long afterSaleId);

}
