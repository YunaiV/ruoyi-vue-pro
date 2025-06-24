package cn.iocoder.yudao.module.huawei.smarthome.controller.app.scenario;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.huawei.smarthome.dto.scenario.*;
import cn.iocoder.yudao.module.huawei.smarthome.service.scenario.HuaweiScenarioService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "用户APP - 华为智能家居 - 场景管理")
@RestController
@RequestMapping("/huawei/smarthome/space/{spaceId}/scenario")
@Validated
public class AppHuaweiScenarioController {

    @Resource
    private HuaweiScenarioService huaweiScenarioService;

    @GetMapping("/list")
    @ApiOperation("查询指定空间下所有场景")
    public CommonResult<ScenarioListRespDTO> listScenariosBySpace(
            @ApiParam(value = "空间 ID", required = true) @PathVariable String spaceId) {
        try {
            ScenarioListBySpaceReqDTO reqDTO = new ScenarioListBySpaceReqDTO(spaceId);
            return success(huaweiScenarioService.listScenariosBySpace(reqDTO));
        } catch (IOException e) {
            return CommonResult.error(500, "查询场景列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/update") // PUT in REST, using POST here
    @ApiOperation("修改场景")
    public CommonResult<?> updateScenario(
            @ApiParam(value = "空间 ID", required = true) @PathVariable String spaceId,
            @Valid @RequestBody ScenarioUpdateReqDTO updateReqDTO) {
        updateReqDTO.setSpaceId(spaceId); // Ensure spaceId from path is used
        try {
            huaweiScenarioService.updateScenario(updateReqDTO);
            return success(null);
        } catch (IOException e) {
            return CommonResult.error(500, "修改场景失败: " + e.getMessage());
        }
    }

    @PostMapping("/{scenarioId}/trigger")
    @ApiOperation("执行场景")
    public CommonResult<?> triggerScenario(
            @ApiParam(value = "空间 ID", required = true) @PathVariable String spaceId,
            @ApiParam(value = "场景 ID", required = true) @PathVariable String scenarioId) {
        try {
            ScenarioTriggerReqDTO reqDTO = new ScenarioTriggerReqDTO(spaceId, scenarioId);
            huaweiScenarioService.triggerScenario(reqDTO);
            return success(null);
        } catch (IOException e) {
            return CommonResult.error(500, "执行场景失败: " + e.getMessage());
        }
    }

    @PostMapping("/{scenarioId}/delete") // DELETE in REST, using POST here
    @ApiOperation("删除场景")
    public CommonResult<?> deleteScenario(
            @ApiParam(value = "空间 ID", required = true) @PathVariable String spaceId,
            @ApiParam(value = "场景 ID", required = true) @PathVariable String scenarioId) {
        try {
            ScenarioDeleteReqDTO reqDTO = new ScenarioDeleteReqDTO(spaceId, scenarioId);
            huaweiScenarioService.deleteScenario(reqDTO);
            return success(null);
        } catch (IOException e) {
            return CommonResult.error(500, "删除场景失败: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    @ApiOperation("导入单个场景")
    public CommonResult<?> importScenario(
            @ApiParam(value = "空间 ID", required = true) @PathVariable String spaceId,
            @Valid @RequestBody ScenarioImportReqDTO importReqDTO) {
        importReqDTO.setSpaceId(spaceId); // Ensure spaceId from path is used
        try {
            huaweiScenarioService.importScenario(importReqDTO);
            return success(null); // Async, result via notification
        } catch (IOException e) {
            return CommonResult.error(500, "导入场景失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    @ApiOperation("场景个数与 ECA 单元统计")
    public CommonResult<ScenarioStatisticsRespDTO> getScenarioStatistics(
            @ApiParam(value = "空间 ID", required = true) @PathVariable String spaceId) {
        try {
            ScenarioStatisticsReqDTO reqDTO = new ScenarioStatisticsReqDTO(spaceId);
            return success(huaweiScenarioService.getScenarioStatistics(reqDTO));
        } catch (IOException e) {
            return CommonResult.error(500, "获取场景统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/{scenarioId}/detail")
    @ApiOperation("查询场景实例详情")
    public CommonResult<ScenarioDetailRespDTO> getScenarioDetail(
            @ApiParam(value = "空间 ID", required = true) @PathVariable String spaceId,
            @ApiParam(value = "场景 ID", required = true) @PathVariable String scenarioId) {
        try {
            ScenarioDetailReqDTO reqDTO = new ScenarioDetailReqDTO(spaceId, scenarioId);
            return success(huaweiScenarioService.getScenarioDetail(reqDTO));
        } catch (IOException e) {
            return CommonResult.error(500, "查询场景详情失败: " + e.getMessage());
        }
    }
}
