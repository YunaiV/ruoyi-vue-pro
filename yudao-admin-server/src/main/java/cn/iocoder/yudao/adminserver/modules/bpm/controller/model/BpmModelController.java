package cn.iocoder.yudao.adminserver.modules.bpm.controller.model;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.BpmModelCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.BpmModelPageItemRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.BpmModelRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.ModelPageReqVO;
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

    @GetMapping ("/page")
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

    @PostMapping("/update")
    @ApiOperation(value = "修改模型属性")
    public CommonResult<String> updateModel(@RequestBody BpmModelCreateReqVO modelVO) {
       return bpmModelService.updateModel(modelVO);
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除模型")
    public CommonResult<String> deleteModel(@RequestParam String modelId) {
       return bpmModelService.deleteModel(modelId);
    }

    @PostMapping("/deploy")
    @ApiOperation(value = "部署模型")
    public CommonResult<String> deploy(@RequestParam String modelId) {
       return bpmModelService.deploy(modelId);
    }

}
