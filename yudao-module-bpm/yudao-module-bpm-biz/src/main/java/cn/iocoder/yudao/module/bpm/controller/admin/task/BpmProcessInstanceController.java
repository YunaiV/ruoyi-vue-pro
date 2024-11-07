package cn.iocoder.yudao.module.bpm.controller.admin.task;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.*;
import cn.iocoder.yudao.module.bpm.convert.task.BpmProcessInstanceConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmCategoryDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.service.definition.BpmCategoryService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 流程实例") // 流程实例，通过流程定义创建的一次“申请”
@RestController
@RequestMapping("/bpm/process-instance")
@Validated
public class BpmProcessInstanceController {

    @Resource
    private BpmProcessInstanceService processInstanceService;
    @Resource
    private BpmTaskService taskService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;
    @Resource
    private BpmCategoryService categoryService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @GetMapping("/my-page")
    @Operation(summary = "获得我的实例分页列表", description = "在【我的流程】菜单中，进行调用")
    @PreAuthorize("@ss.hasPermission('bpm:process-instance:query')")
    public CommonResult<PageResult<BpmProcessInstanceRespVO>> getProcessInstanceMyPage(
            @Valid BpmProcessInstancePageReqVO pageReqVO) {
        PageResult<HistoricProcessInstance> pageResult = processInstanceService.getProcessInstancePage(
                getLoginUserId(), pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接返回
        Map<String, List<Task>> taskMap = taskService.getTaskMapByProcessInstanceIds(
                convertList(pageResult.getList(), HistoricProcessInstance::getId));
        Map<String, ProcessDefinition> processDefinitionMap = processDefinitionService.getProcessDefinitionMap(
                convertSet(pageResult.getList(), HistoricProcessInstance::getProcessDefinitionId));
        Map<String, BpmCategoryDO> categoryMap = categoryService.getCategoryMap(
                convertSet(processDefinitionMap.values(), ProcessDefinition::getCategory));
        return success(BpmProcessInstanceConvert.INSTANCE.buildProcessInstancePage(pageResult,
                processDefinitionMap, categoryMap, taskMap, null, null));
    }

    @GetMapping("/manager-page")
    @Operation(summary = "获得管理流程实例的分页列表", description = "在【流程实例】菜单中，进行调用")
    @PreAuthorize("@ss.hasPermission('bpm:process-instance:manager-query')")
    public CommonResult<PageResult<BpmProcessInstanceRespVO>> getProcessInstanceManagerPage(
            @Valid BpmProcessInstancePageReqVO pageReqVO) {
        PageResult<HistoricProcessInstance> pageResult = processInstanceService.getProcessInstancePage(
                null, pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接返回
        Map<String, List<Task>> taskMap = taskService.getTaskMapByProcessInstanceIds(
                convertList(pageResult.getList(), HistoricProcessInstance::getId));
        Map<String, ProcessDefinition> processDefinitionMap = processDefinitionService.getProcessDefinitionMap(
                convertSet(pageResult.getList(), HistoricProcessInstance::getProcessDefinitionId));
        Map<String, BpmCategoryDO> categoryMap = categoryService.getCategoryMap(
                convertSet(processDefinitionMap.values(), ProcessDefinition::getCategory));
        // 发起人信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(pageResult.getList(), processInstance -> NumberUtils.parseLong(processInstance.getStartUserId())));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        return success(BpmProcessInstanceConvert.INSTANCE.buildProcessInstancePage(pageResult,
                processDefinitionMap, categoryMap, taskMap, userMap, deptMap));
    }

    @PostMapping("/create")
    @Operation(summary = "新建流程实例")
    @PreAuthorize("@ss.hasPermission('bpm:process-instance:query')")
    public CommonResult<String> createProcessInstance(@Valid @RequestBody BpmProcessInstanceCreateReqVO createReqVO) {
        return success(processInstanceService.createProcessInstance(getLoginUserId(), createReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得指定流程实例", description = "在【流程详细】界面中，进行调用")
    @Parameter(name = "id", description = "流程实例的编号", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:process-instance:query')")
    public CommonResult<BpmProcessInstanceRespVO> getProcessInstance(@RequestParam("id") String id) {
        HistoricProcessInstance processInstance = processInstanceService.getHistoricProcessInstance(id);
        if (processInstance == null) {
            return success(null);
        }

        // 拼接返回
        ProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(
                processInstance.getProcessDefinitionId());
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService.getProcessDefinitionInfo(
                processInstance.getProcessDefinitionId());
        String bpmnXml = BpmnModelUtils.getBpmnXml(
                processDefinitionService.getProcessDefinitionBpmnModel(processInstance.getProcessDefinitionId()));
        AdminUserRespDTO startUser = adminUserApi.getUser(NumberUtils.parseLong(processInstance.getStartUserId()));
        DeptRespDTO dept = null;
        if (startUser != null && startUser.getDeptId() != null) {
            dept = deptApi.getDept(startUser.getDeptId());
        }
        return success(BpmProcessInstanceConvert.INSTANCE.buildProcessInstance(processInstance,
                processDefinition, processDefinitionInfo, bpmnXml, startUser, dept));
    }

    @DeleteMapping("/cancel-by-start-user")
    @Operation(summary = "用户取消流程实例", description = "取消发起的流程")
    @PreAuthorize("@ss.hasPermission('bpm:process-instance:cancel')")
    public CommonResult<Boolean> cancelProcessInstanceByStartUser(
            @Valid @RequestBody BpmProcessInstanceCancelReqVO cancelReqVO) {
        processInstanceService.cancelProcessInstanceByStartUser(getLoginUserId(), cancelReqVO);
        return success(true);
    }

    @DeleteMapping("/cancel-by-admin")
    @Operation(summary = "管理员取消流程实例", description = "管理员撤回流程")
    @PreAuthorize("@ss.hasPermission('bpm:process-instance:cancel-by-admin')")
    public CommonResult<Boolean> cancelProcessInstanceByManager(
            @Valid @RequestBody BpmProcessInstanceCancelReqVO cancelReqVO) {
        processInstanceService.cancelProcessInstanceByAdmin(getLoginUserId(), cancelReqVO);
        return success(true);
    }

    @GetMapping("/get-form-fields-permission")
    @Operation(summary = "获得表单字段权限")
    @PreAuthorize("@ss.hasPermission('bpm:process-instance:query')")
    public CommonResult<Map<String, String>> getFormFieldsPermission(
            @Valid BpmFormFieldsPermissionReqVO reqVO) {
        return success(processInstanceService.getFormFieldsPermission(reqVO));
    }

    @GetMapping("/get-approval-detail")
    @Operation(summary = "获得审批详情")
    @Parameter(name = "id", description = "流程实例的编号", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:process-instance:query')")
    public CommonResult<BpmApprovalDetailRespVO> getApprovalDetail(@Valid BpmApprovalDetailReqVO reqVO) {
        return success(processInstanceService.getApprovalDetail(getLoginUserId(), reqVO));
    }

}
