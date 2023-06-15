package cn.iocoder.yudao.module.trade.framework.delivery.core.client;

import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressQueryRespDTO;

import java.util.List;

/**
 * 快递客户端接口
 *
 * @author jason
 */
public interface ExpressClient {

    /**
     * 快递实时查询
     *
     * @param reqDTO 查询请求参数
     */
    // TODO @jason：返回字段可以参考 https://doc.youzanyun.com/detail/API/0/5 响应的 data
    List<ExpressQueryRespDTO> getExpressTrackList(ExpressQueryReqDTO reqDTO);

}
