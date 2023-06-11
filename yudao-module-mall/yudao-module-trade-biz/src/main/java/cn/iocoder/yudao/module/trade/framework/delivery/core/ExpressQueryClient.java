package cn.iocoder.yudao.module.trade.framework.delivery.core;

import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.ExpressQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.ExpressQueryRespDTO;

import java.util.List;

// TODO @jason：可以改成 ExpressClient，未来可能还对接别的接口噢
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
    // TODO @jason：可以改成 getExpressTrackList。返回字段可以参考 https://doc.youzanyun.com/detail/API/0/5 响应的 data
    List<ExpressQueryRespDTO> realTimeQuery(ExpressQueryReqDTO reqDTO);

}
