package cn.iocoder.yudao.module.ai1.controller.admin.home;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageSummaryByAgentRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageSummaryByDateRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeSummaryRespVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.service.home.Ai1HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - AI1 首页")
@RestController
@RequestMapping("/ai1/home")
@Validated
public class Ai1HomeController {

    @Resource
    private Ai1HomeService homeService;
    @Resource
    private Ai1AgentService agentService;

    @GetMapping("/summary")
    @Operation(summary = "获得总量统计")
    @PreAuthorize("@ss.hasPermission('ai1:home:query')")
    public CommonResult<Ai1HomeSummaryRespVO> getSummary() {
        return success(homeService.getSummary());
    }

    @GetMapping("/message-summary-by-date")
    @Operation(summary = "获得按日消息统计", description = "近 days 个自然日（含今天）每日消息数量，没有消息的日期补 0")
    @Parameter(name = "days", description = "天数，1 ~ 30，默认 30", example = "30")
    @PreAuthorize("@ss.hasPermission('ai1:home:query')")
    public CommonResult<List<Ai1HomeMessageSummaryByDateRespVO>> getMessageSummaryByDate(
            @RequestParam(value = "days", defaultValue = "30") @Min(value = 1, message = "天数不能小于 1")
            @Max(value = 30, message = "天数不能大于 30") Integer days) {
        return success(homeService.getMessageSummaryByDate(days));
    }

    @GetMapping("/message-summary-by-agent")
    @Operation(summary = "获得按 Agent 消息统计", description = "近 days 个自然日（含今天）各 Agent 的消息数量")
    @Parameter(name = "days", description = "天数，1 ~ 30，默认 30", example = "30")
    @PreAuthorize("@ss.hasPermission('ai1:home:query')")
    public CommonResult<List<Ai1HomeMessageSummaryByAgentRespVO>> getMessageSummaryByAgent(
            @RequestParam(value = "days", defaultValue = "30") @Min(value = 1, message = "天数不能小于 1")
            @Max(value = 30, message = "天数不能大于 30") Integer days) {
        return success(buildMessageSummaryByAgentRespVOList(homeService.getMessageSummaryByAgent(days)));
    }

    // ==================== 拼接 VO ====================

    private List<Ai1HomeMessageSummaryByAgentRespVO> buildMessageSummaryByAgentRespVOList(List<Ai1HomeMessageSummaryByAgentRespVO> list) {
        Map<Long, Ai1AgentDO> agentMap = agentService.getAgentMap(convertSet(list, Ai1HomeMessageSummaryByAgentRespVO::getAgentId));
        list.forEach(item ->
                MapUtils.findAndThen(agentMap, item.getAgentId(), agent -> item.setAgentName(agent.getName())));
        return list;
    }

}
