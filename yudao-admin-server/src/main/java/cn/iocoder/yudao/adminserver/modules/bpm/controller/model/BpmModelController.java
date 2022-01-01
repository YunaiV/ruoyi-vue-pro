package cn.iocoder.yudao.adminserver.modules.bpm.controller.model;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.BpmModelCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.BpmModelRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.ModelPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.FileResp;
import cn.iocoder.yudao.adminserver.modules.bpm.service.model.BpmModelService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
    @ApiOperation(value = "分页数据")
    public CommonResult<PageResult<BpmModelRespVO>> getModelPage(ModelPageReqVO pageVO) {
       return success(bpmModelService.getModelPage(pageVO));
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

    @GetMapping("/exportBpmnXml")
    @ApiOperation(value = "导出模型Xml")
    public void export(@RequestParam String modelId, HttpServletResponse response) throws IOException {
        FileResp fileResp = bpmModelService.exportBpmnXml(modelId);
        ServletUtils.writeAttachment(response, fileResp.getFileName(), fileResp.getFileByte());
    }

}
