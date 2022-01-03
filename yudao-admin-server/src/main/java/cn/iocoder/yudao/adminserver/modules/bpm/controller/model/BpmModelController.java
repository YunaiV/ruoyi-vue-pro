package cn.iocoder.yudao.adminserver.modules.bpm.controller.model;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.*;
import cn.iocoder.yudao.adminserver.modules.bpm.service.model.BpmModelService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "流程模型")
@RestController
@RequestMapping("/bpm/model")
@Validated
public class BpmModelController {

    @Resource
    private BpmModelService bpmModelService;

    // TODO @芋艿：权限

    @GetMapping("/page")
    @ApiOperation(value = "获得模型分页")
    public CommonResult<PageResult<BpmModelPageItemRespVO>> getModelPage(ModelPageReqVO pageVO) {
        return success(bpmModelService.getModelPage(pageVO));
    }

    @GetMapping("/get")
    @ApiOperation("获得模型")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = String.class)
//    @PreAuthorize("@ss.hasPermission('bpm:form:query')")
    public CommonResult<BpmModelRespVO> getModel(@RequestParam("id") String id) {
        BpmModelRespVO model = bpmModelService.getModel(id);
        return success(model);
    }

    @PostMapping("/create")
    @ApiOperation(value = "新建模型")
    public CommonResult<String> createModel(@RequestBody BpmModelCreateReqVO createRetVO) {
        return success(bpmModelService.createModel(createRetVO));
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改模型")
    public CommonResult<Boolean> updateModel(@RequestBody BpmModelUpdateReqVO modelVO) {
        bpmModelService.updateModel(modelVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除模型")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = String.class)
    public CommonResult<Boolean> deleteModel(@RequestParam("id") String id) {
        bpmModelService.deleteModel(id);
        return success(true);
    }

    @PostMapping("/deploy")
    @ApiOperation(value = "部署模型")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = String.class)
    public CommonResult<Boolean> deployModel(@RequestParam("id") String id) {
        bpmModelService.deployModel(id);
        return success(true);
    }

    @PutMapping("/update-state")
    @ApiOperation(value = "修改模型的状态", notes = "实际更新的部署的流程定义的状态")
    public CommonResult<Boolean> updateModelState(@RequestBody BpmModelUpdateStateReqVO reqVO) {
        bpmModelService.updateModelState(reqVO.getId(), reqVO.getState());
        return success(true);
    }

}
