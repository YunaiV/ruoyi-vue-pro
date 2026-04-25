package cn.iocoder.yudao.module.trade.controller.app.wholesale;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.trade.controller.app.wholesale.vo.AppWholesaleOrderProcessReqVO;
import cn.iocoder.yudao.module.trade.controller.app.wholesale.vo.AppWholesaleOrderProcessRespVO;
import cn.iocoder.yudao.module.trade.service.wholesale.IntelligentOrderProcessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户 App - 智能批发订单处理（IntelligentOrderProcessorService）
 *
 * @author deepay
 */
@Tag(name = "用户 App - 智能批发订单处理")
@RestController
@RequestMapping("/trade/wholesale")
@Validated
public class AppWholesaleOrderController {

    @Resource
    private IntelligentOrderProcessorService intelligentOrderProcessorService;

    @PostMapping("/process")
    @Operation(summary = "处理批发订单（库存检查→智能定价→信用评估→合同→存证）")
    public CommonResult<AppWholesaleOrderProcessRespVO> processOrder(
            @Valid @RequestBody AppWholesaleOrderProcessReqVO reqVO) {
        return success(intelligentOrderProcessorService.processWholesaleOrder(getLoginUserId(), reqVO));
    }

}
