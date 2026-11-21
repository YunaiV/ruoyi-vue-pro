package cn.iocoder.yudao.module.bpm.service.definition;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelSaveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelUpdateReqVO;
import cn.iocoder.yudao.module.bpm.convert.definition.BpmModelConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmBoundaryEventTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskApproveTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskTimeoutHandlerTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmReasonEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceCopyService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ModelQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.BOUNDARY_EVENT_TYPE;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.USER_TASK_TIMEOUT_HANDLER_TYPE;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.USER_TASK_TIMEOUT_JUMP_NODE_ID;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils.parseCandidateStrategy;

/**
 * 流程模型实现：主要进行 Flowable {@link Model} 的维护
 *
 * @author yunlongn
 * @author 芋道源码
 * @author jason
 */
@Service
@Validated
@Slf4j
public class BpmModelServiceImpl implements BpmModelService {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;
    @Resource
    private BpmFormService bpmFormService;

    @Resource
    private BpmTaskCandidateInvoker taskCandidateInvoker;

    @Resource
    private HistoryService historyService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private TaskService taskService;
    @Resource
    private BpmProcessInstanceCopyService processInstanceCopyService;
    @Resource
    private Validator validator;

    @Override
    public List<Model> getModelList(String name) {
        ModelQuery modelQuery = repositoryService.createModelQuery();
        if (StrUtil.isNotEmpty(name)) {
            modelQuery.modelNameLike("%" + name + "%");
        }
        modelQuery.modelTenantId(FlowableUtils.getTenantId());
        return modelQuery.list();
    }

    @Override
    public Long getModelCountByCategory(String category) {
        return repositoryService.createModelQuery()
                .modelCategory(category)
                .modelTenantId(FlowableUtils.getTenantId())
                .count();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createModel(@Valid BpmModelSaveReqVO createReqVO) {
        if (!ValidationUtils.isXmlNCName(createReqVO.getKey())) {
            throw exception(MODEL_KEY_VALID);
        }
        // 1. 校验流程标识已经存在
        Model keyModel = getModelByKey(createReqVO.getKey());
        if (keyModel != null) {
            throw exception(MODEL_KEY_EXISTS, createReqVO.getKey());
        }

        // 2. 创建 Model 对象
        createReqVO.setSort(System.currentTimeMillis()); // 使用当前时间，作为排序
        Model model = repositoryService.newModel();
        BpmModelConvert.INSTANCE.copyToModel(model, createReqVO);
        model.setTenantId(FlowableUtils.getTenantId());

        // 3. 保存模型
        saveModel(model, createReqVO);
        return model.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importModel(@Valid BpmModelSaveReqVO reqVO) {
        if (!ValidationUtils.isXmlNCName(reqVO.getKey())) {
            throw exception(MODEL_KEY_VALID);
        }
        // 1. 校验流程标识已经存在
        Model keyModel = getModelByKey(reqVO.getKey());
        if (keyModel != null) {
            throw exception(MODEL_KEY_EXISTS, reqVO.getKey());
        }

        // 2. 创建 Model 对象，并归属到当前租户
        reqVO.setSort(System.currentTimeMillis());
        Model model = repositoryService.newModel();
        BpmModelConvert.INSTANCE.copyToModel(model, reqVO);
        model.setTenantId(FlowableUtils.getTenantId());

        // 3. 保存模型
        saveModel(model, reqVO);
        return model.getId();
    }

    @Override
    public BpmModelSaveReqVO exportModel(String id) {
        Model model = validateModelExists(id);
        BpmModelSaveReqVO result = BpmModelConvert.INSTANCE.convertToSaveReqVO(model);
        if (Objects.equals(result.getType(), BpmModelTypeEnum.BPMN.getType())) {
            byte[] bpmnBytes = getModelBpmnXML(id);
            result.setBpmnXml(ArrayUtil.isEmpty(bpmnBytes) ? null : StrUtil.utf8Str(bpmnBytes));
        } else if (Objects.equals(result.getType(), BpmModelTypeEnum.SIMPLE.getType())) {
            result.setSimpleModel(JsonUtils.parseObject(getModelSimpleJson(id), BpmSimpleModelNodeVO.class));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 因为进行多个操作，所以开启事务
    public void updateModel(Long userId, BpmModelSaveReqVO updateReqVO) {
        // 1. 校验流程模型存在
        Model model = validateModelManager(updateReqVO.getId(), userId);

        // 2. 填充 Model 信息
        BpmModelConvert.INSTANCE.copyToModel(model, updateReqVO);

        // 3. 保存模型
        saveModel(model, updateReqVO);
    }

    /**
     * 保存模型的基本信息、流程图
     *
     * @param model 模型
     * @param saveReqVO 保存信息
     */
    private void saveModel(Model model, BpmModelSaveReqVO saveReqVO) {
        // 0. 校验 SIMPLE 模型
        if (ObjUtil.equals(BpmModelTypeEnum.SIMPLE.getType(), saveReqVO.getType())) {
            validateSimpleModel(saveReqVO.getSimpleModel());
            validateSimpleModelButtons(saveReqVO.getSimpleModel());
        }

        // 1. 保存模型的基础信息
        repositoryService.saveModel(model);

        // 2. 保存流程图
        if (ObjUtil.equals(BpmModelTypeEnum.BPMN.getType(), saveReqVO.getType())
                && StrUtil.isNotEmpty(saveReqVO.getBpmnXml())) {
            updateModelBpmnXml(model.getId(), saveReqVO.getBpmnXml());
        } else if (ObjUtil.equals(BpmModelTypeEnum.SIMPLE.getType(), saveReqVO.getType())
                && saveReqVO.getSimpleModel() != null) {
            // JSON 转换成 bpmnModel
            BpmnModel bpmnModel = SimpleModelUtils.buildBpmnModel(model.getKey(), model.getName(),
                    saveReqVO.getSimpleModel());
            // 保存 Bpmn XML
            updateModelBpmnXml(model.getId(), BpmnModelUtils.getBpmnXml(bpmnModel));
            // 保存 JSON 数据
            updateModelSimpleJson(model.getId(), saveReqVO.getSimpleModel());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateModelSortBatch(Long userId, List<String> ids) {
        // 1.1 校验流程模型存在
        List<Model> models = repositoryService.createModelQuery()
                .modelTenantId(FlowableUtils.getTenantId()).list();
        models.removeIf(model -> !ids.contains(model.getId()));
        if (ids.size() != models.size()) {
            throw exception(MODEL_NOT_EXISTS);
        }
        Map<String, Model> modelMap = convertMap(models, Model::getId);
        // 1.2 校验是否为管理员
        ids.forEach(id -> validateModelManager(id, userId));

        // 保存排序
        long sort = System.currentTimeMillis(); // 使用时间戳 - i 作为排序
        for (int i = ids.size() - 1; i >= 0; i--) {
            Model model = modelMap.get(ids.get(i));
            // 更新模型
            BpmModelMetaInfoVO metaInfo = BpmModelConvert.INSTANCE.parseMetaInfo(model).setSort(sort);
            model.setMetaInfo(JsonUtils.toJsonString(metaInfo));
            repositoryService.saveModel(model);
            // 更新排序
            processDefinitionService.updateProcessDefinitionSortByModelId(model.getId(), sort);
            sort--;
        }
    }

    private Model validateModelExists(String id) {
        Model model = repositoryService.createModelQuery()
                .modelId(id).modelTenantId(FlowableUtils.getTenantId()).singleResult();
        if (model == null) {
            throw exception(MODEL_NOT_EXISTS);
        }
        return model;
    }

    /**
     * 校验是否有流程模型的管理权限
     *
     * @param id     流程模型编号
     * @param userId 用户编号
     * @return 流程模型
     */
    private Model validateModelManager(String id, Long userId) {
        Model model = validateModelExists(id);
        BpmModelMetaInfoVO metaInfo = BpmModelConvert.INSTANCE.parseMetaInfo(model);
        if (metaInfo == null || !CollUtil.contains(metaInfo.getManagerUserIds(), userId)) {
            throw exception(MODEL_UPDATE_FAIL_NOT_MANAGER, model.getName());
        }
        return model;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 因为进行多个操作，所以开启事务
    public void deployModel(Long userId, String id) {
        // 1.1 校验流程模型存在
        Model model = validateModelManager(id, userId);
        BpmModelMetaInfoVO metaInfo = BpmModelConvert.INSTANCE.parseMetaInfo(model);
        // 1.2 校验流程图
        byte[] bpmnBytes = getModelBpmnXML(model.getId());
        validateBpmnXml(bpmnBytes, metaInfo.getType());
        // 1.3 校验表单已配
        BpmFormDO form = validateFormConfig(metaInfo);
        // 1.4 校验任务分配规则已配置
        taskCandidateInvoker.validateBpmnConfig(bpmnBytes);
        // 1.5 获取仿钉钉流程设计器模型数据，并校验：保存后，跳转的目标节点可能被修改（例如说，导入的模型）
        String simpleJson = getModelSimpleJson(model.getId());
        if (BpmModelTypeEnum.SIMPLE.getType().equals(metaInfo.getType())) {
            BpmSimpleModelNodeVO simpleModel = JsonUtils.parseObject(simpleJson, BpmSimpleModelNodeVO.class);
            validateSimpleModel(simpleModel);
            validateSimpleModelTimeoutJumpMatch(BpmnModelUtils.getBpmnModel(bpmnBytes), simpleModel);
        }

        // 2.1 创建流程定义
        String definitionId = processDefinitionService.createProcessDefinition(model, metaInfo, bpmnBytes, simpleJson,
                form);

        // 2.2 将老的流程定义进行挂起。也就是说，只有最新部署的流程定义，才可以发起任务。
        updateProcessDefinitionSuspended(model.getDeploymentId());

        // 2.3 更新 model 的 deploymentId，进行关联
        ProcessDefinition definition = processDefinitionService.getProcessDefinition(definitionId);
        model.setDeploymentId(definition.getDeploymentId());
        repositoryService.saveModel(model);
    }

    private void validateBpmnXml(byte[] bpmnBytes, Integer type) {
        BpmnModel bpmnModel = BpmnModelUtils.getBpmnModel(bpmnBytes);
        if (bpmnModel == null) {
            throw exception(MODEL_NOT_EXISTS);
        }
        // 1. 没有 StartEvent
        StartEvent startEvent = BpmnModelUtils.getStartEvent(bpmnModel);
        if (startEvent == null) {
            throw exception(MODEL_DEPLOY_FAIL_BPMN_START_EVENT_NOT_EXISTS);
        }
        // 2. 校验 UserTask 的 name 都配置了
        List<UserTask> userTasks = BpmnModelUtils.getBpmnModelElements(bpmnModel, UserTask.class);
        userTasks.forEach(userTask -> {
            if (StrUtil.isEmpty(userTask.getName())) {
                throw exception(MODEL_DEPLOY_FAIL_BPMN_USER_TASK_NAME_NOT_EXISTS, userTask.getId());
            }
        });
        // 3. 校验超时执行动作【自动跳转】：仅 SIMPLE 设计器支持，跳转的目标节点由 SIMPLE 模型配置并校验
        if (BpmModelTypeEnum.BPMN.getType().equals(type)) {
            BoundaryEvent timeoutJumpEvent = CollUtil.getFirst(getTimeoutJumpBoundaryEvents(bpmnModel));
            if (timeoutJumpEvent != null) {
                throw exception(MODEL_DEPLOY_FAIL_TIMEOUT_JUMP_NOT_SIMPLE, getAttachedToName(timeoutJumpEvent));
            }
        }
        // 4. 校验第一个用户任务节点的规则类型是否为“审批人自选”，BPMN 设计器，校验第一个用户任务节点，SIMPLE 设计器，第一个节点固定为发起人所以校验第二个用户任务节点
        UserTask firUserTask = CollUtil.get(userTasks, BpmModelTypeEnum.BPMN.getType().equals(type) ? 0 : 1);
        if (firUserTask == null) {
            return;
        }
        Integer candidateStrategy = parseCandidateStrategy(firUserTask);
        if (Objects.equals(candidateStrategy, BpmTaskCandidateStrategyEnum.APPROVE_USER_SELECT.getStrategy())) {
            throw exception(MODEL_DEPLOY_FAIL_FIRST_USER_TASK_CANDIDATE_STRATEGY_ERROR, firUserTask.getName());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModel(Long userId, String id) {
        // 校验流程模型存在
        Model model = validateModelManager(id, userId);

        // 执行删除
        repositoryService.deleteModel(id);
        // 禁用流程定义
        updateProcessDefinitionSuspended(model.getDeploymentId());
    }

    @Override
    public void cleanModel(Long userId, String id) {
        // 1. 校验流程模型存在
        Model model = validateModelManager(id, userId);

        // 2. 清理所有流程数据
        // 2.1 先取消所有正在运行的流程
        List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(model.getKey()).list();
        processInstances.forEach(processInstance -> {
            runtimeService.deleteProcessInstance(processInstance.getId(),
                    BpmReasonEnum.CANCEL_BY_SYSTEM.getReason());
            historyService.deleteHistoricProcessInstance(processInstance.getId());
            processInstanceCopyService.deleteProcessInstanceCopy(processInstance.getId());
        });
        // 2.2 再从历史中删除所有相关的流程数据
        List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(model.getKey()).list();
        historicProcessInstances.forEach(historicProcessInstance -> {
            historyService.deleteHistoricProcessInstance(historicProcessInstance.getId());
            processInstanceCopyService.deleteProcessInstanceCopy(historicProcessInstance.getId());
        });
        // 2.3 清理所有 Task
        List<Task> tasks = taskService.createTaskQuery()
                .processDefinitionKey(model.getKey()).list();
        tasks.forEach(task -> taskService.deleteTask(task.getId(),BpmReasonEnum.CANCEL_BY_PROCESS_CLEAN.getReason()));
    }

    @Override
    public void updateModelState(Long userId, String id, Integer state) {
        // 1.1 校验流程模型存在
        Model model = validateModelManager(id, userId);
        // 1.2 校验流程定义存在
        ProcessDefinition definition = processDefinitionService
                .getProcessDefinitionByDeploymentId(model.getDeploymentId());
        if (definition == null) {
            throw exception(PROCESS_DEFINITION_NOT_EXISTS);
        }

        // 2. 更新状态
        processDefinitionService.updateProcessDefinitionState(definition.getId(), state);
    }

    @Override
    public BpmnModel getBpmnModelByDefinitionId(String processDefinitionId) {
        return repositoryService.getBpmnModel(processDefinitionId);
    }

    @Override
    public BpmSimpleModelNodeVO getSimpleModel(String modelId) {
        Model model = validateModelExists(modelId);
        // 通过 ACT_RE_MODEL 表 EDITOR_SOURCE_EXTRA_VALUE_ID_ ，获取仿钉钉快搭模型的 JSON 数据
        String json = getModelSimpleJson(model.getId());
        return JsonUtils.parseObject(json, BpmSimpleModelNodeVO.class);
    }

    @Override
    public void updateSimpleModel(Long userId, BpmSimpleModelUpdateReqVO reqVO) {
        // 1. 校验流程模型存在
        Model model = validateModelManager(reqVO.getId(), userId);
        validateSimpleModel(reqVO.getSimpleModel());
        validateSimpleModelButtons(reqVO.getSimpleModel());

        // 2.1 JSON 转换成 bpmnModel
        BpmnModel bpmnModel = SimpleModelUtils.buildBpmnModel(model.getKey(), model.getName(), reqVO.getSimpleModel());
        // 2.2 保存 Bpmn XML
        updateModelBpmnXml(model.getId(), BpmnModelUtils.getBpmnXml(bpmnModel));
        // 2.3 保存 JSON 数据
        updateModelSimpleJson(model.getId(), reqVO.getSimpleModel());
    }

    /**
     * 校验 SIMPLE 模型的审批超时【自动跳转】，与实际部署的流程图一致
     * <p>
     * 为什么需要校验？流程图可以单独修改（例如说，/update-bpmn 接口），只校验 SIMPLE 模型时，流程图中可能多出、缺少、或者替换了跳转的目标节点。
     * 所以，比较两者中【开启自动跳转的节点 - 跳转的目标节点】是否一致，并校验流程图中两个节点实际存在、可以跳转，不比较流程图的其它内容
     *
     * @param bpmnModel   实际部署的流程图
     * @param simpleModel SIMPLE 模型
     */
    private void validateSimpleModelTimeoutJumpMatch(BpmnModel bpmnModel, BpmSimpleModelNodeVO simpleModel) {
        // 1. SIMPLE 模型中，开启自动跳转的节点
        Map<String, BpmSimpleModelNodeVO> simpleNodes = simpleModel == null ? Collections.emptyMap()
                : convertMap(SimpleModelUtils.getTimeoutJumpNodes(simpleModel), BpmSimpleModelNodeVO::getId);
        // 2.1 流程图中，自动跳转的边界事件：节点在 SIMPLE 模型中不存在、同一节点有多个、或者跳转的目标节点不同时，都不一致
        Set<String> bpmnNodeIds = new HashSet<>();
        for (BoundaryEvent boundaryEvent : getTimeoutJumpBoundaryEvents(bpmnModel)) {
            String nodeId = boundaryEvent.getAttachedToRefId();
            String jumpNodeId = BpmnModelUtils.parseBoundaryEventExtensionElement(boundaryEvent, USER_TASK_TIMEOUT_JUMP_NODE_ID);
            BpmSimpleModelNodeVO simpleNode = simpleNodes.get(nodeId);
            if (simpleNode == null || !bpmnNodeIds.add(nodeId)
                    || ObjUtil.notEqual(simpleNode.getTimeoutHandler().getJumpNodeId(), jumpNodeId)) {
                throw exception(MODEL_DEPLOY_FAIL_TIMEOUT_JUMP_NOT_MATCH, getAttachedToName(boundaryEvent));
            }
            // 2.2 流程图中，当前节点、目标节点需要是人工审批的用户任务，目标节点的审批人不能是自选：
            // 流程图中节点编号一致，但节点被删除、或者修改为其它节点时，运行时无法正确跳转。与运行时一样，按节点编号获取
            FlowElement targetElement = BpmnModelUtils.getFlowElementById(bpmnModel, jumpNodeId);
            if (!isTimeoutJumpUserTask(BpmnModelUtils.getFlowElementById(bpmnModel, nodeId))
                    || !isTimeoutJumpUserTask(targetElement)
                    || ObjectUtils.equalsAny(parseCandidateStrategy(targetElement),
                    BpmTaskCandidateStrategyEnum.APPROVE_USER_SELECT.getStrategy(),
                    BpmTaskCandidateStrategyEnum.START_USER_SELECT.getStrategy())) {
                throw exception(MODEL_DEPLOY_FAIL_TIMEOUT_JUMP_NOT_MATCH, getAttachedToName(boundaryEvent));
            }
        }
        // 3. SIMPLE 模型中开启自动跳转，但是流程图中缺少
        simpleNodes.values().forEach(simpleNode -> {
            if (!bpmnNodeIds.contains(simpleNode.getId())) {
                throw exception(MODEL_DEPLOY_FAIL_TIMEOUT_JUMP_NOT_MATCH, simpleNode.getName());
            }
        });
    }

    /**
     * 获得流程图中，超时执行动作为【自动跳转】的用户任务超时边界事件，包括嵌入式子流程中的
     */
    private static List<BoundaryEvent> getTimeoutJumpBoundaryEvents(BpmnModel bpmnModel) {
        return bpmnModel.getProcesses().stream()
                .flatMap(process -> process.findFlowElementsOfType(BoundaryEvent.class, true).stream())
                .filter(boundaryEvent -> ObjUtil.equal(BpmBoundaryEventTypeEnum.USER_TASK_TIMEOUT.getType(),
                        NumberUtils.parseInt(BpmnModelUtils.parseBoundaryEventExtensionElement(boundaryEvent, BOUNDARY_EVENT_TYPE)))
                        && ObjUtil.equal(BpmUserTaskTimeoutHandlerTypeEnum.AUTO_JUMP.getType(), NumberUtils.parseInt(
                        BpmnModelUtils.parseBoundaryEventExtensionElement(boundaryEvent, USER_TASK_TIMEOUT_HANDLER_TYPE))))
                .toList();
    }

    /**
     * 判断流程图中的节点，是否为人工审批的用户任务，可以作为审批超时【自动跳转】的当前节点、目标节点
     * <p>
     * 特殊：办理节点也是人工审批的用户任务，通过节点类型区分，所以需要是审批节点
     */
    private static boolean isTimeoutJumpUserTask(FlowElement element) {
        return element instanceof UserTask
                && ObjUtil.equal(BpmUserTaskApproveTypeEnum.USER.getType(), BpmnModelUtils.parseApproveType(element))
                && ObjUtil.equal(BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType(), BpmnModelUtils.parseNodeType(element));
    }

    private static String getAttachedToName(BoundaryEvent boundaryEvent) {
        return boundaryEvent.getAttachedToRef() != null && StrUtil.isNotEmpty(boundaryEvent.getAttachedToRef().getName())
                ? boundaryEvent.getAttachedToRef().getName() : boundaryEvent.getAttachedToRefId();
    }

    /**
     * 校验 SIMPLE 模型的配置
     * <p>
     * 目前只校验审批超时【自动跳转】的目标节点，范围见 {@link SimpleModelUtils#isValidTimeoutJump}
     *
     * @param simpleModel SIMPLE 模型
     */
    private void validateSimpleModel(BpmSimpleModelNodeVO simpleModel) {
        if (simpleModel == null) {
            return;
        }
        BpmSimpleModelNodeVO invalidNode = SimpleModelUtils.findInvalidTimeoutJumpNode(simpleModel);
        if (invalidNode != null) {
            throw exception(MODEL_SAVE_FAIL_TIMEOUT_JUMP_NODE_INVALID, invalidNode.getName());
        }
    }

    /**
     * 校验 SIMPLE 模型所有节点的操作按钮，例如说按钮类型编号
     * <p>
     * 为什么需要遍历？请求参数只级联校验到根节点的按钮，子节点（childNode、conditionNodes）未级联校验，避免一并校验节点的其它约束。
     * 所以，遍历所有节点，只按 {@link BpmSimpleModelNodeVO.OperationButtonSetting} 自身的约束校验
     *
     * @param simpleModel SIMPLE 模型
     */
    private void validateSimpleModelButtons(BpmSimpleModelNodeVO simpleModel) {
        List<BpmSimpleModelNodeVO.OperationButtonSetting> buttons = new ArrayList<>();
        SimpleModelUtils.traverseNode(simpleModel, node -> CollUtil.emptyIfNull(node.getButtonsSetting())
                .stream().filter(Objects::nonNull).forEach(buttons::add));
        buttons.forEach(button -> ValidationUtils.validate(validator, button));
    }

    /**
     * 校验流程表单已配置
     *
     * @param metaInfo 流程模型元数据
     * @return 表单配置
     */
    private BpmFormDO validateFormConfig(BpmModelMetaInfoVO metaInfo) {
        if (metaInfo == null || metaInfo.getFormType() == null) {
            throw exception(MODEL_DEPLOY_FAIL_FORM_NOT_CONFIG);
        }
        // 校验表单存在
        if (Objects.equals(metaInfo.getFormType(), BpmModelFormTypeEnum.NORMAL.getType())) {
            if (metaInfo.getFormId() == null) {
                throw exception(MODEL_DEPLOY_FAIL_FORM_NOT_CONFIG);
            }
            BpmFormDO form = bpmFormService.getForm(metaInfo.getFormId());
            if (form == null) {
                throw exception(FORM_NOT_EXISTS);
            }
            return form;
        } else {
            if (StrUtil.isEmpty(metaInfo.getFormCustomCreatePath())
                    || StrUtil.isEmpty(metaInfo.getFormCustomViewPath())) {
                throw exception(MODEL_DEPLOY_FAIL_FORM_NOT_CONFIG);
            }
            return null;
        }
    }

    @Override
    public void updateModelBpmnXml(String id, String bpmnXml) {
        if (StrUtil.isEmpty(bpmnXml)) {
            return;
        }
        repositoryService.addModelEditorSource(id, StrUtil.utf8Bytes(bpmnXml));
    }

    @SuppressWarnings("JavaExistingMethodCanBeUsed")
    private String getModelSimpleJson(String id) {
        byte[] bytes = repositoryService.getModelEditorSourceExtra(id);
        if (ArrayUtil.isEmpty(bytes)) {
            return null;
        }
        return StrUtil.utf8Str(bytes);
    }

    private void updateModelSimpleJson(String id, BpmSimpleModelNodeVO node) {
        if (node == null) {
            return;
        }
        byte[] bytes = JsonUtils.toJsonByte(node);
        repositoryService.addModelEditorSourceExtra(id, bytes);
    }

    /**
     * 挂起 deploymentId 对应的流程定义
     * <p>
     * 注意：这里一个 deploymentId 只关联一个流程定义
     *
     * @param deploymentId 流程发布Id
     */
    private void updateProcessDefinitionSuspended(String deploymentId) {
        if (StrUtil.isEmpty(deploymentId)) {
            return;
        }
        ProcessDefinition oldDefinition = processDefinitionService.getProcessDefinitionByDeploymentId(deploymentId);
        if (oldDefinition == null) {
            return;
        }
        processDefinitionService.updateProcessDefinitionState(oldDefinition.getId(),
                SuspensionState.SUSPENDED.getStateCode());
    }

    private Model getModelByKey(String key) {
        return repositoryService.createModelQuery()
                .modelTenantId(FlowableUtils.getTenantId())
                .modelKey(key).singleResult();
    }

    @Override
    public Model getModel(String id) {
        return repositoryService.createModelQuery()
                .modelId(id).modelTenantId(FlowableUtils.getTenantId()).singleResult();
    }

    @Override
    public byte[] getModelBpmnXML(String id) {
        return repositoryService.getModelEditorSource(id);
    }

}
