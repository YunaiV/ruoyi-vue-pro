package cn.iocoder.yudao.adminserver.modules.bpm.controller.definition;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.BpmProcessDefinitionPageItemRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.BpmProcessDefinitionPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.FileResp;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmDefinitionService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "流程定义")
@RestController
@RequestMapping("/bpm/definition")
@Validated
public class ProcessDefinitionController {

    @Resource
    private BpmDefinitionService bpmDefinitionService;

    // TODO 芋艿：权限

    @GetMapping ("/page")
    @ApiOperation(value = "获得流程定义分页")
    public CommonResult<PageResult<BpmProcessDefinitionPageItemRespVO>> getDefinitionPage(BpmProcessDefinitionPageReqVO pageReqVO) {
        return success(bpmDefinitionService.getDefinitionPage(pageReqVO));
    }

    @GetMapping(value = "/getStartForm")
    public CommonResult<String> getStartForm(@RequestParam("processKey") String processKey){
//        final ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().
//                processDefinitionKey(processKey).latestVersion().singleResult();
//        processRuntime.processDefinition(processDefinition.getId()).getFormKey();
        // TODO 这样查似乎有问题？？， 暂时写死
        return success("/flow/leave/apply");
    }

    @GetMapping ("/export")
    @ApiOperation(value = "流程定义的bpmnXml导出")
    public void getDefinitionPage(@RequestParam String processDefinitionId, HttpServletResponse response) throws IOException {
        FileResp fileResp = bpmDefinitionService.export(processDefinitionId);
        ServletUtils.writeAttachment(response, fileResp.getFileName(), fileResp.getFileByte());
    }

}
