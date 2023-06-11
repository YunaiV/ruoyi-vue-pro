package cn.iocoder.yudao.module.trade.framework.delivery.core;

import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.ExpressQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.ExpressQueryRespDTO;

import java.util.List;

/**
 * 快递查询客户端
 *
 * @author jason
 */
public interface ExpressQueryClient {

    /**
     * 快递实时查询
     *
     * @param reqDTO 查询请求参数
     */
    List<ExpressQueryRespDTO> realTimeQuery(ExpressQueryReqDTO reqDTO);
}
