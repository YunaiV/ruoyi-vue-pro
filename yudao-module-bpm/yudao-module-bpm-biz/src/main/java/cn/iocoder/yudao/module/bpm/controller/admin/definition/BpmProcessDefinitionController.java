package cn.iocoder.yudao.module.bpm.controller.admin.definition;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionRespVO;
import cn.iocoder.yudao.module.bpm.convert.definition.BpmProcessDefinitionConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmCategoryDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.strategy.BpmTaskCandidateStartUserSelectStrategy;
import cn.iocoder.yudao.module.bpm.service.definition.BpmCategoryService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmFormService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 流程定义")
@RestController
@RequestMapping("/bpm/process-definition")
@Validated
public class BpmProcessDefinitionController {

    @Resource
    private BpmProcessDefinitionService processDefinitionService;
    @Resource
    private BpmFormService formService;
    @Resource
    private BpmCategoryService categoryService;

    @GetMapping("/page")
    @Operation(summary = "获得流程定义分页")
    @PreAuthorize("@ss.hasPermission('bpm:process-definition:query')")
    public CommonResult<PageResult<BpmProcessDefinitionRespVO>> getProcessDefinitionPage(
            BpmProcessDefinitionPageReqVO pageReqVO) {
        PageResult<ProcessDefinition> pageResult = processDefinitionService.getProcessDefinitionPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 获得 Category Map
        Map<String, BpmCategoryDO> categoryMap = categoryService.getCategoryMap(
                convertSet(pageResult.getList(), ProcessDefinition::getCategory));
        // 获得 Deployment Map
        Map<String, Deployment> deploymentMap = processDefinitionService.getDeploymentMap(
                convertSet(pageResult.getList(), ProcessDefinition::getDeploymentId));
        // 获得 BpmProcessDefinitionInfoDO Map
        Map<String, BpmProcessDefinitionInfoDO> processDefinitionMap = processDefinitionService.getProcessDefinitionInfoMap(
                convertSet(pageResult.getList(), ProcessDefinition::getId));
        // 获得 Form Map
        Map<Long, BpmFormDO> formMap = formService.getFormMap(
               convertSet(processDefinitionMap.values(), BpmProcessDefinitionInfoDO::getFormId));
        return success(BpmProcessDefinitionConvert.INSTANCE.buildProcessDefinitionPage(
                pageResult, deploymentMap, processDefinitionMap, formMap, categoryMap));
    }

    @GetMapping ("/list")
    @Operation(summary = "获得流程定义列表")
    @Parameter(name = "suspensionState", description = "挂起状态", required = true, example = "1") // 参见 Flowable SuspensionState 枚举
    public CommonResult<List<BpmProcessDefinitionRespVO>> getProcessDefinitionList(
            @RequestParam("suspensionState") Integer suspensionState) {
        List<ProcessDefinition> list = processDefinitionService.getProcessDefinitionListBySuspensionState(suspensionState);
        if (CollUtil.isEmpty(list)) {
            return success(Collections.emptyList());
        }

        // 获得 BpmProcessDefinitionInfoDO Map
        Map<String, BpmProcessDefinitionInfoDO> processDefinitionMap = processDefinitionService.getProcessDefinitionInfoMap(
                convertSet(list, ProcessDefinition::getId));
        return success(BpmProcessDefinitionConvert.INSTANCE.buildProcessDefinitionList(
                list, null, processDefinitionMap, null, null));
    }

    @GetMapping ("/get")
    @Operation(summary = "获得流程定义")
    @Parameter(name = "id", description = "流程编号", required = true, example = "1024")
    @Parameter(name = "key", description = "流程定义标识", required = true, example = "1024")
    public CommonResult<BpmProcessDefinitionRespVO> getProcessDefinition(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "key", required = false) String key) {
        ProcessDefinition processDefinition = id != null ? processDefinitionService.getProcessDefinition(id)
                : processDefinitionService.getActiveProcessDefinition(key);
        if (processDefinition == null) {
            return success(null);
        }
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService.getProcessDefinitionInfo(processDefinition.getId());
        BpmnModel bpmnModel = processDefinitionService.getProcessDefinitionBpmnModel(processDefinition.getId());
        List<UserTask> userTaskList = BpmTaskCandidateStartUserSelectStrategy.getStartUserSelectUserTaskList(bpmnModel);
        return success(BpmProcessDefinitionConvert.INSTANCE.buildProcessDefinition(
                processDefinition, null, processDefinitionInfo, null, null, bpmnModel, userTaskList));
    }

}
