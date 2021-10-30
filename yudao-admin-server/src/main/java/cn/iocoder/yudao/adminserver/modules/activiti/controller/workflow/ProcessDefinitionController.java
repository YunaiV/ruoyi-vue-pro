package cn.iocoder.yudao.adminserver.modules.activiti.controller.workflow;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/workflow/process/definition")
public class ProcessDefinitionController {

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private ProcessRuntime processRuntime;


    @GetMapping(value = "/getStartForm")
    public CommonResult<String> getStartForm(@RequestParam("processKey") String processKey){
        //这样查似乎有问题？？， 暂时写死
//        final ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().
//                processDefinitionKey(processKey).latestVersion().singleResult();
//        processRuntime.processDefinition(processDefinition.getId()).getFormKey();
        return CommonResult.success("/flow/leave/apply");
    }
}
