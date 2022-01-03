package cn.iocoder.yudao.adminserver.modules.bpm.controller.definition;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.BpmProcessDefinitionPageItemRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.BpmProcessDefinitionPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmDefinitionService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "流程定义")
@RestController
@RequestMapping("/bpm/definition")
@Validated
public class BpmDefinitionController {

    @Resource
    private BpmDefinitionService bpmDefinitionService;

    @GetMapping ("/page")
    @ApiOperation(value = "获得流程定义分页")
    @PreAuthorize("@ss.hasPermission('bpm:model:query')") // 暂时使用 model 的权限标识
    public CommonResult<PageResult<BpmProcessDefinitionPageItemRespVO>> getDefinitionPage(BpmProcessDefinitionPageReqVO pageReqVO) {
        return success(bpmDefinitionService.getDefinitionPage(pageReqVO));
    }

    // TODO 芋艿：需要重写该方法
    @GetMapping(value = "/getStartForm")
    public CommonResult<String> getStartForm(@RequestParam("processKey") String processKey){
//        final ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().
//                processDefinitionKey(processKey).latestVersion().singleResult();
//        processRuntime.processDefinition(processDefinition.getId()).getFormKey();
        // TODO 这样查似乎有问题？？， 暂时写死
        return success("/flow/leave/apply");
    }

    @GetMapping ("/get-bpmn-xml")
    @ApiOperation(value = "获得流程定义的 BPMN XML")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = String.class)
    @PreAuthorize("@ss.hasPermission('bpm:model:query')") // 暂时使用 model 的权限标识
    public CommonResult<String> getDefinitionBpmnXML(@RequestParam("id") String id) {
        String bpmnXML = bpmDefinitionService.getDefinitionBpmnXML(id);
        return success(bpmnXML);
    }

}
