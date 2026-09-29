package cn.iocoder.yudao.module.ai1.controller.admin.home;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageShareRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageTrendRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeSummaryRespVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.service.home.Ai1HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
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

    @GetMapping("/message-trend")
    @Operation(summary = "获得消息趋势", description = "近 days 个自然日（含今天）每日消息数量，没有消息的日期补 0")
    @Parameter(name = "days", description = "天数，1~30，默认 30", example = "30")
    @PreAuthorize("@ss.hasPermission('ai1:home:query')")
    public CommonResult<List<Ai1HomeMessageTrendRespVO>> getMessageTrend(
            @RequestParam(value = "days", required = false) Integer days) {
        return success(homeService.getMessageTrend(days));
    }

    // TODO @AI：message-summary？相关的类，方法名，是不是都处理下？
    @GetMapping("/message-share")
    @Operation(summary = "获得 Agent 消息占比", description = "近 days 个自然日（含今天）各 Agent 的消息数量")
    @Parameter(name = "days", description = "天数，1~30，默认 30", example = "30")
    @PreAuthorize("@ss.hasPermission('ai1:home:query')")
    public CommonResult<List<Ai1HomeMessageShareRespVO>> getMessageShare(
            @RequestParam(value = "days", required = false) Integer days) {
        return success(buildMessageShareRespVOList(homeService.getMessageShare(days)));
    }

    // ==================== 拼接 VO ====================

    private List<Ai1HomeMessageShareRespVO> buildMessageShareRespVOList(List<Ai1HomeMessageShareRespVO> list) {
        // TODO @AI：是不是 getAgentList map，有个 default 方法；
        Map<Long, Ai1AgentDO> agentMap = convertMap(agentService.getAgentList(convertSet(list, Ai1HomeMessageShareRespVO::getAgentId)), Ai1AgentDO::getId);
        // TODO @AI：前端处理 agentName 的兜底把？后端只返回就行了把。
        list.forEach(item -> item.setAgentName(agentMap.containsKey(item.getAgentId())
                ? agentMap.get(item.getAgentId()).getName() : StrUtil.format("Agent#{}", item.getAgentId())));
        return list;
    }

}
