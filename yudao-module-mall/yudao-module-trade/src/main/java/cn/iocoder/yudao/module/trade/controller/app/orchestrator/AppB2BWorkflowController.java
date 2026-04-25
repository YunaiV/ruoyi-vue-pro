package cn.iocoder.yudao.module.trade.controller.app.orchestrator;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.trade.controller.app.orchestrator.vo.AppB2BWorkflowReqVO;
import cn.iocoder.yudao.module.trade.controller.app.wholesale.vo.AppWholesaleOrderProcessReqVO;
import cn.iocoder.yudao.module.trade.service.orchestrator.B2BWorkflowOrchestratorService;
import cn.iocoder.yudao.module.trade.service.orchestrator.bo.WorkflowResultBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户 App - B2B 全流程工作流编排
 * <p>
 * 官方 SDK 集成：ProductSkuApi → IntelligentOrderProcessor → BlockchainAsyncService(JD Chain) → ProductShareService → ProductRecommendService
 *
 * @author deepay
 */
@Tag(name = "用户 App - B2B 工作流编排")
@RestController
@RequestMapping("/trade/workflow")
@Validated
public class AppB2BWorkflowController {

    @Resource
    private B2BWorkflowOrchestratorService workflowOrchestratorService;

    @PostMapping("/execute")
    @Operation(summary = "执行完整 B2B 批发工作流（验证→定价→存证→分享→推荐）")
    public CommonResult<WorkflowResultBO> executeWorkflow(@Valid @RequestBody AppB2BWorkflowReqVO reqVO) {
        // 将 VO 转换为 IntelligentOrderProcessor 所需的请求 VO
        AppWholesaleOrderProcessReqVO processReq = new AppWholesaleOrderProcessReqVO();
        processReq.setRemark(reqVO.getRemark());
        processReq.setAddressId(reqVO.getAddressId());
        processReq.setItems(reqVO.getItems().stream().map(item -> {
            AppWholesaleOrderProcessReqVO.Item i = new AppWholesaleOrderProcessReqVO.Item();
            i.setSkuId(item.getSkuId());
            i.setCount(item.getCount());
            return i;
        }).collect(Collectors.toList()));

        WorkflowResultBO result = workflowOrchestratorService.executeWholesaleWorkflow(
                getLoginUserId(), processReq);
        return success(result);
    }

}
