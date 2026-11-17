package cn.iocoder.yudao.module.ai.controller.admin.drama;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskActionReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskRespVO;
import cn.iocoder.yudao.module.ai.service.drama.AiDramaTaskService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - AI 短剧生成任务")
@RestController
@RequestMapping("/ai/drama/task")
public class AiDramaTaskController {

    @Resource
    private AiDramaTaskService taskService;

    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/page")
    @Operation(summary = "获得生成任务分页", description = "由角色形象、场景、视觉资产、镜头视频、成片的状态汇总")
    @PreAuthorize("@ss.hasPermission('ai:drama:query')")
    public CommonResult<PageResult<AiDramaTaskRespVO>> getTaskPage(@Valid AiDramaTaskPageReqVO pageReqVO) {
        PageResult<AiDramaTaskRespVO> pageResult = taskService.getTaskPage(pageReqVO);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(pageResult.getList(), AiDramaTaskRespVO::getUserId));
        pageResult.getList().forEach(task -> MapUtils.findAndThen(userMap, task.getUserId(),
                user -> task.setUserNickname(user.getNickname())));
        return success(pageResult);
    }

    @PutMapping("/resubmit")
    @Operation(summary = "重新提交", description = "只支持已失败的任务；使用上次的模型")
    @PreAuthorize("@ss.hasPermission('ai:drama:task')")
    public CommonResult<Boolean> resubmitTask(@Valid @RequestBody AiDramaTaskActionReqVO reqVO) {
        taskService.resubmitTask(reqVO);
        return success(true);
    }

    @PutMapping("/sync")
    @Operation(summary = "同步状态", description = "只支持生成中、转存中的镜头视频")
    @PreAuthorize("@ss.hasPermission('ai:drama:task')")
    public CommonResult<Boolean> syncTask(@Valid @RequestBody AiDramaTaskActionReqVO reqVO) {
        taskService.syncTask(reqVO);
        return success(true);
    }

    @PutMapping("/update-fail")
    @Operation(summary = "标记失败", description = "只支持进行中的任务")
    @PreAuthorize("@ss.hasPermission('ai:drama:task')")
    public CommonResult<Boolean> updateTaskFail(@Valid @RequestBody AiDramaTaskActionReqVO reqVO) {
        taskService.updateTaskFail(reqVO);
        return success(true);
    }

}
