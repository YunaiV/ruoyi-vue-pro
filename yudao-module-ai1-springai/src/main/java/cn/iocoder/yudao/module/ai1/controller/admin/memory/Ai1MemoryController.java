package cn.iocoder.yudao.module.ai1.controller.admin.memory;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.memory.vo.Ai1MemoryPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.memory.vo.Ai1MemoryRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.memory.vo.Ai1MemoryUpdateReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.memory.Ai1MemoryDO;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import cn.iocoder.yudao.module.ai1.service.memory.Ai1MemoryService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - AI1 长期记忆")
@RestController
@RequestMapping("/ai1/memory")
@Validated
public class Ai1MemoryController {

    @Resource
    private Ai1MemoryService memoryService;
    @Resource
    private Ai1AgentService agentService;

    @Resource
    private AdminUserApi adminUserApi;

    @PutMapping("/update")
    @Operation(summary = "更新长期记忆")
    @PreAuthorize("@ss.hasPermission('ai1:memory:update')")
    public CommonResult<Boolean> updateMemory(@Valid @RequestBody Ai1MemoryUpdateReqVO updateReqVO) {
        memoryService.updateMemory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除长期记忆", description = "之后根据历史对话重新生成")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:memory:delete')")
    public CommonResult<Boolean> deleteMemory(@RequestParam("id") Long id) {
        memoryService.deleteMemory(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除长期记忆", description = "之后根据历史对话重新生成")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:memory:delete')")
    public CommonResult<Boolean> deleteMemoryList(@RequestParam("ids") List<Long> ids) {
        memoryService.deleteMemoryListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得长期记忆")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai1:memory:query')")
    public CommonResult<Ai1MemoryRespVO> getMemory(@RequestParam("id") Long id) {
        Ai1MemoryDO memory = memoryService.getMemory(id);
        return success(buildMemoryRespVO(memory));
    }

    @GetMapping("/page")
    @Operation(summary = "获得长期记忆分页")
    @PreAuthorize("@ss.hasPermission('ai1:memory:query')")
    public CommonResult<PageResult<Ai1MemoryRespVO>> getMemoryPage(@Valid Ai1MemoryPageReqVO pageReqVO) {
        PageResult<Ai1MemoryDO> pageResult = memoryService.getMemoryPage(pageReqVO);
        return success(new PageResult<>(buildMemoryRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    private Ai1MemoryRespVO buildMemoryRespVO(Ai1MemoryDO memory) {
        if (memory == null) {
            return null;
        }
        return CollUtil.getFirst(buildMemoryRespVOList(Collections.singletonList(memory)));
    }

    private List<Ai1MemoryRespVO> buildMemoryRespVOList(List<Ai1MemoryDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, Ai1AgentDO> agentMap = agentService.getAgentMap(convertSet(list, Ai1MemoryDO::getAgentId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(list, Ai1MemoryDO::getUserId));
        return BeanUtils.toBean(list, Ai1MemoryRespVO.class, respVO -> {
            MapUtils.findAndThen(agentMap, respVO.getAgentId(), agent -> respVO.setAgentName(agent.getName()));
            MapUtils.findAndThen(userMap, respVO.getUserId(), user -> respVO.setUserNickname(user.getNickname()));
        });
    }

}
