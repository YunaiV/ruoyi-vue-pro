package cn.iocoder.yudao.module.ai.controller.admin.image;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionTaskCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionTaskPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionTaskRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionTaskDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionTaskStepDO;
import cn.iocoder.yudao.module.ai.service.image.AiFashionTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 管理后台 - AI 服装设计流水线任务
 *
 * <p>提供多模型流水线（SDXL → Pose → Fabric → Upscale）的 RESTful API：
 * 创建任务、查询任务（含步骤）、分页、重试。</p>
 *
 * @author deepay
 */
@Tag(name = "管理后台 - AI 服装设计流水线")
@RestController
@RequestMapping("/ai/fashion/task")
@Validated
@Slf4j
public class AiFashionTaskController {

    @Resource
    private AiFashionTaskService fashionTaskService;

    // ========== 核心接口 ==========

    @PostMapping("/create")
    @Operation(summary = "创建服装设计任务", description = "立即返回 taskId，任务异步执行；前端通过 GET /get 轮询进度")
    public CommonResult<Long> createTask(@Valid @RequestBody AiFashionTaskCreateReqVO createVO) {
        Long taskId = fashionTaskService.createTask(getLoginUserId(), createVO);
        return success(taskId);
    }

    @GetMapping("/get")
    @Operation(summary = "查询任务详情（含步骤列表）")
    @Parameter(name = "id", description = "任务编号", required = true, example = "1")
    public CommonResult<AiFashionTaskRespVO> getTask(@RequestParam("id") Long id) {
        AiFashionTaskDO task = fashionTaskService.getTask(id);
        if (task == null) {
            return success(null);
        }
        List<AiFashionTaskStepDO> steps = fashionTaskService.getTaskSteps(id);
        return success(convertToRespVO(task, steps));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询任务列表")
    @PreAuthorize("@ss.hasPermission('ai:fashion-task:query')")
    public CommonResult<PageResult<AiFashionTaskRespVO>> getTaskPage(@Validated AiFashionTaskPageReqVO pageReqVO) {
        PageResult<AiFashionTaskDO> pageResult = fashionTaskService.getTaskPage(pageReqVO);
        List<AiFashionTaskRespVO> voList = pageResult.getList().stream()
                .map(task -> convertToRespVO(task, null))
                .collect(Collectors.toList());
        return success(new PageResult<>(voList, pageResult.getTotal()));
    }

    @PostMapping("/retry")
    @Operation(summary = "重试失败任务", description = "仅失败状态的任务可重试，将从头重新执行全部步骤")
    @Parameter(name = "id", description = "任务编号", required = true, example = "1")
    public CommonResult<Boolean> retryTask(@RequestParam("id") Long id) {
        fashionTaskService.retryTask(id);
        return success(true);
    }

    // ========== 私有工具方法 ==========

    private AiFashionTaskRespVO convertToRespVO(AiFashionTaskDO task, List<AiFashionTaskStepDO> steps) {
        AiFashionTaskRespVO vo = new AiFashionTaskRespVO();
        vo.setId(task.getId());
        vo.setUserId(task.getUserId());
        vo.setPrompt(task.getPrompt());
        vo.setNegativePrompt(task.getNegativePrompt());
        vo.setWidth(task.getWidth());
        vo.setHeight(task.getHeight());
        vo.setSeed(task.getSeed());
        vo.setQualityPreset(task.getQualityPreset());
        vo.setModelCheckpoint(task.getModelCheckpoint());
        vo.setPoseImageUrl(task.getPoseImageUrl());
        vo.setFabricRefUrl(task.getFabricRefUrl());
        vo.setFabricStrength(task.getFabricStrength());
        vo.setUpscale(task.getUpscale());
        vo.setUpscaleFactor(task.getUpscaleFactor());
        vo.setUpscalerName(task.getUpscalerName());
        vo.setStatus(task.getStatus());
        vo.setTraceId(task.getTraceId());
        vo.setFinalPicUrl(task.getFinalPicUrl());
        vo.setErrorMessage(task.getErrorMessage());
        vo.setStartTime(task.getStartTime());
        vo.setFinishTime(task.getFinishTime());
        vo.setCreateTime(task.getCreateTime());

        if (steps != null) {
            vo.setSteps(steps.stream().map(this::convertStepVO).collect(Collectors.toList()));
        }
        return vo;
    }

    private AiFashionTaskRespVO.Step convertStepVO(AiFashionTaskStepDO step) {
        AiFashionTaskRespVO.Step vo = new AiFashionTaskRespVO.Step();
        vo.setId(step.getId());
        vo.setStepOrder(step.getStepOrder());
        vo.setStepType(step.getStepType());
        vo.setStatus(step.getStatus());
        vo.setInputPicUrl(step.getInputPicUrl());
        vo.setOutputPicUrl(step.getOutputPicUrl());
        vo.setInputOptions(step.getInputOptions());
        vo.setOutputOptions(step.getOutputOptions());
        vo.setModelName(step.getModelName());
        vo.setDurationMs(step.getDurationMs());
        vo.setRetryCount(step.getRetryCount());
        vo.setErrorMessage(step.getErrorMessage());
        vo.setCreateTime(step.getCreateTime());
        return vo;
    }

}
