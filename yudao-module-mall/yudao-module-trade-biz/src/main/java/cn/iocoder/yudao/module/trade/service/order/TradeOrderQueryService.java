package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackRespDTO;

import java.util.List;

/**
 * 交易订单【读】 Service 接口
 *
 * @author 芋道源码
 */
public interface TradeOrderQueryService {

    /**
     * 获得订单的物流轨迹
     *
     * @param id 订单编号
     * @param userId 用户编号
     * @return 物流轨迹数组
     */
    List<ExpressTrackRespDTO> getExpressTrackList(Long id, Long userId);

}
