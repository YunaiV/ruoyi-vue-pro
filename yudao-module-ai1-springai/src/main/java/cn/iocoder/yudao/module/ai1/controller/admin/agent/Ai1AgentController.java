package cn.iocoder.yudao.module.ai1.controller.admin.agent;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.agent.vo.Ai1AgentPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.agent.vo.Ai1AgentRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.agent.vo.Ai1AgentSaveReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.agent.vo.Ai1AgentUpdateStatusReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.provider.Ai1ModelDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.provider.Ai1ProviderDO;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.service.provider.Ai1ModelService;
import cn.iocoder.yudao.module.ai1.service.provider.Ai1ProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - AI1 Agent")
@RestController
@RequestMapping("/ai1/agent")
@Validated
public class Ai1AgentController {

    @Resource
    private Ai1AgentService agentService;
    @Resource
    private Ai1ProviderService providerService;
    @Resource
    private Ai1ModelService modelService;

    @PostMapping("/create")
    @Operation(summary = "创建 Agent")
    @PreAuthorize("@ss.hasPermission('ai1:agent:create')")
    public CommonResult<Long> createAgent(@Valid @RequestBody Ai1AgentSaveReqVO createReqVO) {
        return success(agentService.createAgent(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 Agent")
    @PreAuthorize("@ss.hasPermission('ai1:agent:update')")
    public CommonResult<Boolean> updateAgent(@Valid @RequestBody Ai1AgentSaveReqVO updateReqVO) {
        agentService.updateAgent(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 Agent", description = "级联删除对话与消息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:agent:delete')")
    public CommonResult<Boolean> deleteAgent(@RequestParam("id") Long id) {
        agentService.deleteAgent(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除 Agent", description = "级联删除对话与消息")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:agent:delete')")
    public CommonResult<Boolean> deleteAgentList(@RequestParam("ids") List<Long> ids) {
        agentService.deleteAgentListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 Agent")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai1:agent:query')")
    public CommonResult<Ai1AgentRespVO> getAgent(@RequestParam("id") Long id) {
        Ai1AgentDO agent = agentService.getAgent(id);
        // TODO @AI：抽个方法？参考别下别的模块；
        if (agent == null) {
            return success(null);
        }
        return success(CollUtil.getFirst(buildAgentRespVOList(Collections.singletonList(agent))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 Agent 分页")
    @PreAuthorize("@ss.hasPermission('ai1:agent:query')")
    public CommonResult<PageResult<Ai1AgentRespVO>> getAgentPage(@Valid Ai1AgentPageReqVO pageReqVO) {
        PageResult<Ai1AgentDO> pageResult = agentService.getAgentPage(pageReqVO);
        return success(new PageResult<>(buildAgentRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得已开启的 Agent 精简列表", description = "用于对话页选择 Agent，仅返回已开启的 Agent")
    @PreAuthorize("@ss.hasPermission('ai1:chat:query')")
    // TODO @AI：simple-list 不用 ai1:chat:query 把？别的也检查下；
    public CommonResult<List<Ai1AgentRespVO>> getAgentSimpleList() {
        List<Ai1AgentRespVO> list = buildAgentRespVOList(agentService.getAgentListByStatus(CommonStatusEnum.ENABLE.getStatus()));
        // TODO @AI：你看看，还有哪些字段要去掉的
        list.forEach(agent -> agent.setSystemPrompt(null));
        return success(list);
    }

    // TODO @AI：不用生成 uuid 噢。
    @PutMapping("/update-status")
    @Operation(summary = "修改 Agent 状态", description = "首次开启时生成访问 UUID")
    @PreAuthorize("@ss.hasPermission('ai1:agent:update')")
    public CommonResult<Boolean> updateAgentStatus(@Valid @RequestBody Ai1AgentUpdateStatusReqVO reqVO) {
        agentService.updateAgentStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private List<Ai1AgentRespVO> buildAgentRespVOList(List<Ai1AgentDO> list) {
        // TODO @AI：default map；都搞搞；
        Map<Long, Ai1ProviderDO> providerMap = convertMap(providerService.getProviderList(
                convertSet(list, Ai1AgentDO::getProviderId)), Ai1ProviderDO::getId);
        Map<Long, Ai1ModelDO> modelMap = convertMap(modelService.getModelList(
                convertSet(list, Ai1AgentDO::getModelId)), Ai1ModelDO::getId);
        return BeanUtils.toBean(list, Ai1AgentRespVO.class, respVO -> {
            MapUtils.findAndThen(providerMap, respVO.getProviderId(), provider -> respVO.setProviderName(provider.getName()));
            MapUtils.findAndThen(modelMap, respVO.getModelId(), model -> respVO.setModelName(model.getName()));
        });
    }

}
