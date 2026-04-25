package cn.iocoder.yudao.module.trade.service.orchestrator;

import cn.iocoder.yudao.module.trade.controller.app.wholesale.vo.AppWholesaleOrderProcessReqVO;
import cn.iocoder.yudao.module.trade.service.orchestrator.bo.WorkflowResultBO;

/**
 * B2B 工作流编排服务
 * <p>
 * 对应 Python B2BWorkflowOrchestrator：
 * 商品验证 → 智能定价 → 订单创建 → 区块链存证 → 分享链接 → 下一步推荐
 *
 * @author deepay
 */
public interface B2BWorkflowOrchestratorService {

    /**
     * 执行完整的 B2B 批发工作流
     *
     * @param userId 当前用户编号
     * @param reqVO  批发订单请求
     * @return 工作流执行结果
     */
    WorkflowResultBO executeWholesaleWorkflow(Long userId, AppWholesaleOrderProcessReqVO reqVO);

}
