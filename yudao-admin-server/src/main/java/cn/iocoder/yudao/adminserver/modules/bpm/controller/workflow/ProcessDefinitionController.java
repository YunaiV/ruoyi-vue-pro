package cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.FileResp;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.processdefinition.ProcessDefinitionPageReqVo;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.processdefinition.ProcessDefinitionRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmDefinitionService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import io.swagger.annotations.ApiOperation;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.engine.RepositoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// TODO @json：swagger 和 validation 的注解，后续要补全下哈。可以等 workflow 基本写的差不多之后
@RestController
@RequestMapping("/workflow/process/definition")
public class ProcessDefinitionController {

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private ProcessRuntime processRuntime;
    @Resource
    private BpmDefinitionService bpmProcessDefinitionService;


    @GetMapping(value = "/getStartForm")
    public CommonResult<String> getStartForm(@RequestParam("processKey") String processKey){
//        final ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().
//                processDefinitionKey(processKey).latestVersion().singleResult();
//        processRuntime.processDefinition(processDefinition.getId()).getFormKey();
        //这样查似乎有问题？？， 暂时写死
        return CommonResult.success("/flow/leave/apply");
    }

    @GetMapping ("/page")
    @ApiOperation(value = "流程定义分页数据")
    public CommonResult<PageResult<ProcessDefinitionRespVO>> pageList(ProcessDefinitionPageReqVo processDefinitionPageReqVo) {
        return CommonResult.success(bpmProcessDefinitionService.pageList(processDefinitionPageReqVo));
    }

    @GetMapping ("/export")
    @ApiOperation(value = "流程定义的bpmnXml导出")
    public void pageList(@RequestParam String processDefinitionId, HttpServletResponse response) throws IOException {
        FileResp fileResp = bpmProcessDefinitionService.export(processDefinitionId);
        ServletUtils.writeAttachment(response, fileResp.getFileName(), fileResp.getFileByte());
    }

}
