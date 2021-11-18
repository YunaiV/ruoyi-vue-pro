package cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.FileResp;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelPageReqVo;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelVO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.workflow.BpmModelService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.repository.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 工作流模型
 * @author yunlongn
 */
@Slf4j
@RestController
@RequestMapping("/workflow/models")
@Api(tags = "工作流模型")
@RequiredArgsConstructor
public class ModelController {

    private final BpmModelService bpmModelService;

    @GetMapping ("/page")
    @ApiOperation(value = "分页数据")
    public CommonResult<PageResult<Model>> pageList(ModelPageReqVo modelPageReqVo) {
       return CommonResult.success(bpmModelService.pageList(modelPageReqVo));
    }

    @PostMapping("/create")
    @ApiOperation(value = "新建模型")
    public CommonResult<String> newModel(@RequestBody ModelVO modelVO) {
       return bpmModelService.newModel(modelVO);
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改模型属性")
    public CommonResult<String> updateModel(@RequestBody ModelVO modelVO) {
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
