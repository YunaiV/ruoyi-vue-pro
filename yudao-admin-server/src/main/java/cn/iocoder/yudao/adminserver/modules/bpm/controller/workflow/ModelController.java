package cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.FileResp;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.TodoTaskRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelCreateVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelPageReqVo;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelRespVo;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelUpdateVO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.workflow.BpmModelService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
    public CommonResult<String> newModel(@RequestBody ModelCreateVO modelCreateVO) {
       return bpmModelService.newModel(modelCreateVO);
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改模型属性")
    public CommonResult<String> updateModel(@RequestBody ModelUpdateVO modelUpdateVO) {
       return bpmModelService.updateModel(modelUpdateVO);
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
    @ApiOperation(value = "导出模型")
    public void export(@RequestParam String deploymentId, HttpServletResponse response) throws IOException {
        FileResp fileResp = bpmModelService.exportBpmnXml(deploymentId);
        ServletUtils.writeAttachment(response, fileResp.getFileName(), fileResp.getFileByte());
    }

}
