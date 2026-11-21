package cn.iocoder.yudao.module.bpm.service.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.*;
import cn.iocoder.yudao.module.bpm.convert.task.BpmProcessInstanceConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.dal.redis.BpmProcessIdRedisDAO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmReasonEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.event.BpmProcessInstanceEventPublisher;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmHttpRequestUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.module.bpm.service.message.BpmMessageService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO.ActivityNode;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;

/**
 * 流程实例【写】Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class BpmProcessInstanceUpdateServiceImpl implements BpmProcessInstanceUpdateService {

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private BpmProcessDefinitionService processDefinitionService;
    @Resource
    private BpmProcessInstanceQueryService processInstanceQueryService;
    @Resource
    @Lazy // 避免循环依赖
    private BpmTaskUpdateService taskUpdateService;
    @Resource
    private BpmMessageService messageService;

    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private BpmProcessInstanceEventPublisher processInstanceEventPublisher;

    @Resource
    private BpmProcessIdRedisDAO processIdRedisDAO;

    // ========== Update 写入相关方法 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(enable = false) // 关闭数据权限，避免查询不到用户数据。相关案例：https://gitee.com/zhijiantianya/yudao-cloud/issues/ID1UYA
    public String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqVO createReqVO) {
        // 获得流程定义
        ProcessDefinition definition = processDefinitionService
                .getProcessDefinition(createReqVO.getProcessDefinitionId());
        // 校验动态表单必填字段（前端校验可被接口直调绕过，后端必须兜底，否则必填缺失也会建成脏实例）
        validateDynamicFormRequiredFields(definition, createReqVO.getVariables());
        // 发起流程
        return createProcessInstance0(userId, definition, createReqVO.getVariables(), null,
                createReqVO.getStartUserSelectAssignees());
    }

    /**
     * 校验动态表单的必填字段
     *
     * @see <a href="https://github.com/YunaiV/ruoyi-vue-pro/pull/1245">相关 issue</a>
     */
    private void validateDynamicFormRequiredFields(ProcessDefinition definition, Map<String, Object> variables) {
        // 流程定义不存在时，交给 createProcessInstance0 统一校验，避免空指针
        if (definition == null) {
            return;
        }
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService
                .getProcessDefinitionInfo(definition.getId());
        if (processDefinitionInfo == null
                || !BpmModelFormTypeEnum.NORMAL.getType().equals(processDefinitionInfo.getFormType())
                || CollUtil.isEmpty(processDefinitionInfo.getFormFields())) {
            return;
        }
        List<String> fieldStrs = processDefinitionInfo.getFormFields();
        // formFields 是 JSON 数组，每个元素本身又是 JSON 字符串；两种表单设计器并存：
        // 字段标识可能是 field 或 key，必填标记可能是数组 ["required"] 或对象 {"required": true}
        for (String fieldStr : fieldStrs) {
            Map<String, Object> field = JsonUtils.parseMap(fieldStr);
            boolean required = false;
            Object validate = field.get("validate");
            if (validate instanceof List) {
                required = ((List<?>) validate).contains("required");
            } else if (validate instanceof Map) {
                required = Boolean.TRUE.equals(((Map<?, ?>) validate).get("required"));
            }
            if (!required) {
                continue;
            }
            String fieldId = Convert.toStr(field.get("field"), Convert.toStr(field.get("key")));
            if (StrUtil.isEmpty(fieldId)) {
                continue;
            }
            Object value = variables == null ? null : variables.get(fieldId);
            if (value == null || (value instanceof String && StrUtil.isBlank((String) value))) {
                throw exception(PROCESS_INSTANCE_START_FORM_FIELD_REQUIRED, Convert.toStr(field.get("title"), fieldId));
            }
        }
    }

    @Override
    @DataPermission(enable = false) // 关闭数据权限，避免查询不到用户数据。相关案例：https://gitee.com/zhijiantianya/yudao-cloud/issues/ID1UYA
    public String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqDTO createReqDTO) {
        return FlowableUtils.executeAuthenticatedUserId(userId, () -> {
            // 获得流程定义
            ProcessDefinition definition = processDefinitionService
                    .getActiveProcessDefinition(createReqDTO.getProcessDefinitionKey());
            // 发起流程
            return createProcessInstance0(userId, definition, createReqDTO.getVariables(),
                    createReqDTO.getBusinessKey(),
                    createReqDTO.getStartUserSelectAssignees());
        });
    }

    private String createProcessInstance0(Long userId, ProcessDefinition definition,
                                          Map<String, Object> variables, String businessKey,
                                          Map<String, List<Long>> startUserSelectAssignees) {
        // 1.1 校验流程定义
        if (definition == null) {
            throw exception(PROCESS_DEFINITION_NOT_EXISTS);
        }
        if (definition.isSuspended()) {
            throw exception(PROCESS_DEFINITION_IS_SUSPENDED);
        }
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService
                .getProcessDefinitionInfo(definition.getId());
        if (processDefinitionInfo == null) {
            throw exception(PROCESS_DEFINITION_NOT_EXISTS);
        }
        // 1.2 校验是否能够发起
        if (!processDefinitionService.canUserStartProcessDefinition(processDefinitionInfo, userId)) {
            throw exception(PROCESS_INSTANCE_START_USER_CAN_START);
        }
        // 1.3 校验发起人自选审批人
        validateStartUserSelectAssignees(userId, definition, startUserSelectAssignees, variables);

        // 2. 创建流程实例
        if (variables == null) {
            variables = new HashMap<>();
        }
        FlowableUtils.filterProcessInstanceFormVariable(variables); // 过滤一下，避免 ProcessInstance 系统级的变量被占用
        variables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_ID, userId); // 设置流程变量，发起人 ID
        variables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS, // 流程实例状态：审批中
                BpmProcessInstanceStatusEnum.RUNNING.getStatus());
        variables.put(BpmnVariableConstants.PROCESS_INSTANCE_SKIP_EXPRESSION_ENABLED, true); // 跳过表达式需要添加此变量为 true，不影响没配置 skipExpression 的节点
        if (CollUtil.isNotEmpty(startUserSelectAssignees)) {
            // 设置流程变量，发起人自选审批人
            variables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_SELECT_ASSIGNEES,
                    startUserSelectAssignees);
        }

        // 3. 创建流程
        ProcessInstanceBuilder processInstanceBuilder = runtimeService.createProcessInstanceBuilder()
                .processDefinitionId(definition.getId())
                .businessKey(businessKey)
                .variables(variables);
        // 3.1 创建流程 ID
        BpmModelMetaInfoVO.ProcessIdRule processIdRule = processDefinitionInfo.getProcessIdRule();
        if (processIdRule != null && Boolean.TRUE.equals(processIdRule.getEnable())) {
            processInstanceBuilder.predefineProcessInstanceId(processIdRedisDAO.generate(processIdRule));
        }
        // 3.2 流程名称
        processInstanceBuilder.name(generateProcessInstanceName(userId, definition, processDefinitionInfo, variables));
        // 3.3 发起流程实例
        ProcessInstance instance = processInstanceBuilder.start();
        return instance.getId();
    }

    private void validateStartUserSelectAssignees(Long userId, ProcessDefinition definition,
                                                  Map<String, List<Long>> startUserSelectAssignees,
                                                  Map<String, Object> variables) {
        // 1. 获取预测的节点信息
        BpmApprovalDetailRespVO detailRespVO = processInstanceQueryService.getApprovalDetail(userId, new BpmApprovalDetailReqVO()
                .setProcessDefinitionId(definition.getId())
                .setProcessVariables(variables));
        List<ActivityNode> activityNodes = detailRespVO.getActivityNodes();
        if (CollUtil.isEmpty(activityNodes)) {
            return;
        }

        // 2.1 移除掉不是发起人自选审批人节点
        activityNodes.removeIf(task ->
                ObjectUtil.notEqual(BpmTaskCandidateStrategyEnum.START_USER_SELECT.getStrategy(), task.getCandidateStrategy()));
        // 2.2 流程发起时要先获取当前流程的预测走向节点，发起时只校验预测的节点发起人自选审批人的审批人和抄送人是否都配置了
        activityNodes.forEach(task -> {
            List<Long> assignees = startUserSelectAssignees != null ? startUserSelectAssignees.get(task.getId()) : null;
            if (CollUtil.isEmpty(assignees)) {
                throw exception(PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_NOT_CONFIG, task.getName());
            }
            Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(assignees);
            assignees.forEach(assignee -> {
                if (userMap.get(assignee) == null) {
                    throw exception(PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_NOT_EXISTS, task.getName(), assignee);
                }
            });
        });
    }

    private String generateProcessInstanceName(Long userId,
                                               ProcessDefinition definition,
                                               BpmProcessDefinitionInfoDO definitionInfo,
                                               Map<String, Object> variables) {
        if (definition == null || definitionInfo == null) {
            return null;
        }
        BpmModelMetaInfoVO.TitleSetting titleSetting = definitionInfo.getTitleSetting();
        if (titleSetting == null || !BooleanUtil.isTrue(titleSetting.getEnable())) {
            return definition.getName();
        }
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        Map<String, Object> cloneVariables = new HashMap<>(variables);
        cloneVariables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_ID, user.getNickname());
        cloneVariables.put(BpmnVariableConstants.PROCESS_START_TIME, DateUtil.now());
        cloneVariables.put(BpmnVariableConstants.PROCESS_DEFINITION_NAME, definition.getName().trim());
        return StrUtil.format(definitionInfo.getTitleSetting().getTitle(), cloneVariables);
    }

    @Override
    @DataPermission(enable = false) // 关闭数据权限，避免查询不到用户数据。相关案例：https://gitee.com/zhijiantianya/yudao-cloud/issues/ID1UYA
    public void cancelProcessInstanceByStartUser(Long userId, @Valid BpmProcessInstanceCancelReqVO cancelReqVO) {
        // 1.1 校验流程实例存在
        ProcessInstance instance = processInstanceQueryService.getProcessInstance(cancelReqVO.getId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_CANCEL_FAIL_NOT_EXISTS);
        }
        // 1.2 只能取消自己的
        if (!Objects.equals(instance.getStartUserId(), String.valueOf(userId))) {
            throw exception(PROCESS_INSTANCE_CANCEL_FAIL_NOT_SELF);
        }
        // 1.3 校验允许撤销审批中的申请
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService
                .getProcessDefinitionInfo(instance.getProcessDefinitionId());
        Assert.notNull(processDefinitionInfo, "流程定义({})不存在", processDefinitionInfo);
        if (processDefinitionInfo.getAllowCancelRunningProcess() != null // 防止未配置 AllowCancelRunningProcess , 默认为可取消
                && BooleanUtil.isFalse(processDefinitionInfo.getAllowCancelRunningProcess())) {
            throw exception(PROCESS_INSTANCE_CANCEL_FAIL_NOT_ALLOW);
        }
        // 1.4 子流程不允许取消
        if (StrUtil.isNotBlank(instance.getSuperExecutionId())) {
            throw exception(PROCESS_INSTANCE_CANCEL_CHILD_FAIL_NOT_ALLOW);
        }

        // 2. 取消流程
        updateProcessInstanceCancel(cancelReqVO.getId(),
                BpmReasonEnum.CANCEL_PROCESS_INSTANCE_BY_START_USER.format(cancelReqVO.getReason()));
    }

    @Override
    @DataPermission(enable = false) // 关闭数据权限，避免查询不到用户数据。相关案例：https://gitee.com/zhijiantianya/yudao-cloud/issues/ID1UYA
    public void cancelProcessInstanceByAdmin(Long userId, BpmProcessInstanceCancelReqVO cancelReqVO) {
        // 1.1 校验流程实例存在
        ProcessInstance instance = processInstanceQueryService.getProcessInstance(cancelReqVO.getId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_CANCEL_FAIL_NOT_EXISTS);
        }

        // 2. 取消流程
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        updateProcessInstanceCancel(cancelReqVO.getId(),
                BpmReasonEnum.CANCEL_PROCESS_INSTANCE_BY_ADMIN.format(user.getNickname(), cancelReqVO.getReason()));
    }

    private void updateProcessInstanceCancel(String id, String reason) {
        // 1. 更新流程实例 status
        runtimeService.setVariable(id, BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS,
                BpmProcessInstanceStatusEnum.CANCEL.getStatus());
        runtimeService.setVariable(id, BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_REASON, reason);

        // 2. 取消所有子流程
        List<ProcessInstance> childProcessInstances = runtimeService.createProcessInstanceQuery()
                .superProcessInstanceId(id).list();
        childProcessInstances.forEach(processInstance -> updateProcessInstanceCancel(
                processInstance.getProcessInstanceId(), BpmReasonEnum.CANCEL_CHILD_PROCESS_INSTANCE_BY_MAIN_PROCESS.getReason()));

        // 3. 结束流程
        taskUpdateService.moveTaskToEnd(id, reason);
        // 特殊：没有运行中的任务时（例如停留在延迟器、触发器），moveTaskToEnd 无法结束流程，直接删除流程实例
        // 相关 issue：https://github.com/YunaiV/ruoyi-vue-pro/issues/966
        if (runtimeService.createProcessInstanceQuery().processInstanceId(id).count() > 0) {
            runtimeService.deleteProcessInstance(id, reason);
        }
    }

    @Override
    public void updateProcessInstanceReject(ProcessInstance processInstance, String reason) {
        runtimeService.setVariable(processInstance.getProcessInstanceId(),
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS,
                BpmProcessInstanceStatusEnum.REJECT.getStatus());
        runtimeService.setVariable(processInstance.getProcessInstanceId(),
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_REASON,
                BpmReasonEnum.REJECT_TASK.format(reason));
    }

    @Override
    public void updateProcessInstanceVariables(String id, Map<String, Object> variables) {
        runtimeService.setVariables(id, variables);
    }

    @Override
    public void removeProcessInstanceVariables(String id, Collection<String> variableNames) {
        runtimeService.removeVariables(id, variableNames);
    }

    // ========== Event 事件相关方法 ==========

    @Override
    public void processProcessInstanceCompleted(ProcessInstance instance) {
        // 1.1 获取当前状态
        Integer status = (Integer) instance.getProcessVariables()
                .get(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS);
        String reason = (String) instance.getProcessVariables()
                .get(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_REASON);
        // 1.2 当流程状态还是审批状态中，说明审批通过了，则变更下它的状态
        // 为什么这么处理？因为流程完成，并且完成了，说明审批通过了
        if (Objects.equals(status, BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
            status = BpmProcessInstanceStatusEnum.APPROVE.getStatus();
            runtimeService.setVariable(instance.getId(), BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS,
                    status);
        }

        // 1.3 如果子流程拒绝，设置其父流程也为拒绝状态，且结束父流程
        // 相关问题链接：https://t.zsxq.com/kZhyb
        if (Objects.equals(status, BpmProcessInstanceStatusEnum.REJECT.getStatus())
                && StrUtil.isNotBlank(instance.getSuperExecutionId())) {
            // 1.3.1 获取父流程实例 并标记为不通过
            Execution execution = runtimeService.createExecutionQuery().executionId(instance.getSuperExecutionId()).singleResult();
            ProcessInstance parentProcessInstance = processInstanceQueryService.getProcessInstance(execution.getProcessInstanceId());
            updateProcessInstanceReject(parentProcessInstance, BpmReasonEnum.REJECT_CHILD_PROCESS.getReason());

            // 1.3.2 结束父流程。需要在子流程结束事务提交后执行
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCompletion(int transactionStatus) {
                    // 回滚情况，直接返回
                    if (ObjectUtil.equal(transactionStatus, TransactionSynchronization.STATUS_ROLLED_BACK)) {
                        return;
                    }
                    taskUpdateService.moveTaskToEnd(parentProcessInstance.getId(), BpmReasonEnum.REJECT_CHILD_PROCESS.getReason());
                }
            });
        }

        // 2. 发送对应的消息通知
        if (Objects.equals(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
            messageService.sendMessageWhenProcessInstanceApprove(
                    BpmProcessInstanceConvert.INSTANCE.buildProcessInstanceApproveMessage(instance));
        } else if (Objects.equals(status, BpmProcessInstanceStatusEnum.REJECT.getStatus())) {
            messageService.sendMessageWhenProcessInstanceReject(
                    BpmProcessInstanceConvert.INSTANCE.buildProcessInstanceRejectMessage(instance, reason));
        }

        // 3. 发送流程实例的状态事件
        processInstanceEventPublisher.sendProcessInstanceResultEvent(
                BpmProcessInstanceConvert.INSTANCE.buildProcessInstanceStatusEvent(this, instance, status, reason));

        // 4. 流程后置通知
        if (Objects.equals(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
            BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService.
                    getProcessDefinitionInfo(instance.getProcessDefinitionId());
            if (ObjUtil.isNotNull(processDefinitionInfo) &&
                    ObjUtil.isNotNull(processDefinitionInfo.getProcessAfterTriggerSetting())) {
                BpmModelMetaInfoVO.HttpRequestSetting setting = processDefinitionInfo.getProcessAfterTriggerSetting();

                BpmHttpRequestUtils.executeBpmHttpRequest(instance,
                        setting.getUrl(), setting.getHeader(), setting.getBody(), true, setting.getResponse());
            }
        }
    }

    @Override
    public void processProcessInstanceCreated(ProcessInstance instance) {
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService.
                getProcessDefinitionInfo(instance.getProcessDefinitionId());
        ProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(instance.getProcessDefinitionId());
        if (processDefinition == null || processDefinitionInfo == null) {
            return;
        }

        // 自定义标题。目的：主要处理子流程的标题无法处理
        // 注意：必须使用 TransactionSynchronizationManager 事务提交后，否则不生效！！！
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                String name = generateProcessInstanceName(Long.valueOf(instance.getStartUserId()),
                        processDefinition, processDefinitionInfo, instance.getProcessVariables());
                if (ObjUtil.notEqual(instance.getName(), name)) {
                    runtimeService.setProcessInstanceName(instance.getProcessInstanceId(), name);
                }

                // 流程前置通知：需要在流程启动后(事务提交后)，保证 variables 已设置
                // 相关问题链接：https://t.zsxq.com/DF7Kq
                if (ObjUtil.isNull(processDefinitionInfo.getProcessBeforeTriggerSetting())) {
                    return;
                }
                BpmModelMetaInfoVO.HttpRequestSetting setting = processDefinitionInfo.getProcessBeforeTriggerSetting();
                BpmHttpRequestUtils.executeBpmHttpRequest(instance,
                        setting.getUrl(), setting.getHeader(), setting.getBody(), true, setting.getResponse());
            }

        });
    }


}
