package cn.iocoder.yudao.module.bpm.controller.admin.definition;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.simple.BpmSimpleModelSaveReqVO;
import cn.iocoder.yudao.module.bpm.service.definition.BpmSimpleModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - BPM 仿钉钉流程设计器")
@RestController
@RequestMapping("/bpm/simple")
public class BpmSimpleModelController {
    @Resource
    private BpmSimpleModelService bpmSimpleModelService;

    @PostMapping("/save")
    @Operation(summary = "保存仿钉钉流程设计模型")
    @PreAuthorize("@ss.hasPermission('bpm:model:update')")
    public CommonResult<Boolean> saveSimpleModel(@Valid @RequestBody BpmSimpleModelSaveReqVO reqVO) {
        return success(bpmSimpleModelService.saveSimpleModel(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得仿钉钉流程设计模型")
    @Parameter(name = "modelId", description = "流程模型编号", required = true, example = "a2c5eee0-eb6c-11ee-abf4-0c37967c420a")
    public CommonResult<BpmSimpleModelNodeVO> getSimpleModel(@RequestParam("modelId") String modelId){
        return success(bpmSimpleModelService.getSimpleModel(modelId));
    }

}
