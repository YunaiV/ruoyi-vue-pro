package cn.iocoder.yudao.module.bpm.service.definition;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelSaveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelUpdateReqVO;
import cn.iocoder.yudao.module.bpm.convert.definition.BpmModelConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ModelQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;

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

    @Override
    public List<Model> getModelList(String name) {
        ModelQuery modelQuery = repositoryService.createModelQuery();
        if (StrUtil.isNotEmpty(name)) {
            modelQuery.modelNameLike("%" + name + "%");
        }
        return modelQuery.list();
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
        for (int i = ids.size() - 1; i > 0; i--) {
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
        Model model = repositoryService.getModel(id);
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
        // 1.2 校验流程图
        byte[] bpmnBytes = getModelBpmnXML(model.getId());
        validateBpmnXml(bpmnBytes);
        // 1.3 校验表单已配
        BpmModelMetaInfoVO metaInfo = BpmModelConvert.INSTANCE.parseMetaInfo(model);
        BpmFormDO form = validateFormConfig(metaInfo);
        // 1.4 校验任务分配规则已配置
        taskCandidateInvoker.validateBpmnConfig(bpmnBytes);
        // 1.5 获取仿钉钉流程设计器模型数据
        String simpleJson = getModelSimpleJson(model.getId());

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

    private void validateBpmnXml(byte[] bpmnBytes) {
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

        // 2.1 JSON 转换成 bpmnModel
        BpmnModel bpmnModel = SimpleModelUtils.buildBpmnModel(model.getKey(), model.getName(), reqVO.getSimpleModel());
        // 2.2 保存 Bpmn XML
        updateModelBpmnXml(model.getId(), BpmnModelUtils.getBpmnXml(bpmnModel));
        // 2.3 保存 JSON 数据
        updateModelSimpleJson(model.getId(), reqVO.getSimpleModel());
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
        return repositoryService.getModel(id);
    }

    @Override
    public byte[] getModelBpmnXML(String id) {
        return repositoryService.getModelEditorSource(id);
    }

}
