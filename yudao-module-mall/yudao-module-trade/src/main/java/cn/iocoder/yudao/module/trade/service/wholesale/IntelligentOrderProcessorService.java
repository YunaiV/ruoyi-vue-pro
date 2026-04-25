package cn.iocoder.yudao.module.trade.service.wholesale;

import cn.iocoder.yudao.module.trade.controller.app.wholesale.vo.AppWholesaleOrderProcessReqVO;
import cn.iocoder.yudao.module.trade.controller.app.wholesale.vo.AppWholesaleOrderProcessRespVO;

/**
 * 智能批发订单处理服务
 * <p>
 * 对应 Python IntelligentOrderProcessorService：
 * 库存检查 → 智能定价 → 信用评估 → 智能合同 → 区块链存证 → 下一步建议
 *
 * @author deepay
 */
public interface IntelligentOrderProcessorService {

    /**
     * 处理批发订单（完整流程）
     *
     * @param userId 当前用户编号
     * @param reqVO  请求 VO
     * @return 处理结果
     */
    AppWholesaleOrderProcessRespVO processWholesaleOrder(Long userId, AppWholesaleOrderProcessReqVO reqVO);

}
