package cn.iocoder.yudao.module.bpm.service.definition;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelUpdateReqVO;
import cn.iocoder.yudao.module.bpm.convert.definition.BpmModelConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.service.definition.dto.BpmModelMetaInfoRespDTO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;

/**
 * Flowable流程模型实现
 * 主要进行 Flowable {@link Model} 的维护
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
    public PageResult<Model> getModelPage(BpmModelPageReqVO pageVO) {
        ModelQuery modelQuery = repositoryService.createModelQuery();
        modelQuery.modelTenantId(FlowableUtils.getTenantId());
        if (StrUtil.isNotBlank(pageVO.getKey())) {
            modelQuery.modelKey(pageVO.getKey());
        }
        if (StrUtil.isNotBlank(pageVO.getName())) {
            modelQuery.modelNameLike("%" + pageVO.getName() + "%"); // 模糊匹配
        }
        if (StrUtil.isNotBlank(pageVO.getCategory())) {
            modelQuery.modelCategory(pageVO.getCategory());
        }
        // 执行查询
        long count = modelQuery.count();
        if (count == 0) {
            return PageResult.empty(count);
        }
        List<Model> models = modelQuery
                .orderByCreateTime().desc()
                .listPage(PageUtils.getStart(pageVO), pageVO.getPageSize());
        return new PageResult<>(models, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createModel(@Valid BpmModelCreateReqVO createReqVO, String bpmnXml) {
        if (!ValidationUtils.isXmlNCName(createReqVO.getKey())) {
            throw exception(MODEL_KEY_VALID);
        }
        // 校验流程标识已经存在
        Model keyModel = getModelByKey(createReqVO.getKey());
        if (keyModel != null) {
            throw exception(MODEL_KEY_EXISTS, createReqVO.getKey());
        }

        // 创建流程定义
        Model model = repositoryService.newModel();
        BpmModelConvert.INSTANCE.copyToCreateModel(model, createReqVO);
        model.setTenantId(FlowableUtils.getTenantId());
        // 保存流程定义
        repositoryService.saveModel(model);
        // 保存 BPMN XML
        saveModelBpmnXml(model, bpmnXml);
        return model.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 因为进行多个操作，所以开启事务
    public void updateModel(@Valid BpmModelUpdateReqVO updateReqVO) {
        // 校验流程模型存在
        Model model = getModel(updateReqVO.getId());
        if (model == null) {
            throw exception(MODEL_NOT_EXISTS);
        }

        // 修改流程定义
        BpmModelConvert.INSTANCE.copyToUpdateModel(model, updateReqVO);
        // 更新模型
        repositoryService.saveModel(model);
        // 更新 BPMN XML
        saveModelBpmnXml(model, updateReqVO.getBpmnXml());
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 因为进行多个操作，所以开启事务
    public void deployModel(String id) {
        // 1.1 校验流程模型存在
        Model model = getModel(id);
        if (ObjectUtils.isEmpty(model)) {
            throw exception(MODEL_NOT_EXISTS);
        }
        // 1.2 校验流程图
        byte[] bpmnBytes = getModelBpmnXML(model.getId());
        validateBpmnXml(bpmnBytes);
        // 1.3 校验表单已配
        BpmModelMetaInfoRespDTO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoRespDTO.class);
        BpmFormDO form = validateFormConfig(metaInfo);
        // 1.4 校验任务分配规则已配置
        taskCandidateInvoker.validateBpmnConfig(bpmnBytes);

        // 2.1 创建流程定义
        String definitionId = processDefinitionService.createProcessDefinition(model, metaInfo, bpmnBytes, form);

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
    public void deleteModel(String id) {
        // 校验流程模型存在
        Model model = getModel(id);
        if (model == null) {
            throw exception(MODEL_NOT_EXISTS);
        }
        // 执行删除
        repositoryService.deleteModel(id);
        // 禁用流程定义
        updateProcessDefinitionSuspended(model.getDeploymentId());
    }

    @Override
    public void updateModelState(String id, Integer state) {
        // 1.1 校验流程模型存在
        Model model = getModel(id);
        if (model == null) {
            throw exception(MODEL_NOT_EXISTS);
        }
        // 1.2 校验流程定义存在
        ProcessDefinition definition = processDefinitionService.getProcessDefinitionByDeploymentId(model.getDeploymentId());
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

    /**
     * 校验流程表单已配置
     *
     * @param metaInfo 流程模型元数据
     * @return 表单配置
     */
    private BpmFormDO validateFormConfig(BpmModelMetaInfoRespDTO  metaInfo) {
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
            if (StrUtil.isEmpty(metaInfo.getFormCustomCreatePath()) || StrUtil.isEmpty(metaInfo.getFormCustomViewPath())) {
                throw exception(MODEL_DEPLOY_FAIL_FORM_NOT_CONFIG);
            }
            return null;
        }
    }

    private void saveModelBpmnXml(Model model, String bpmnXml) {
        if (StrUtil.isEmpty(bpmnXml)) {
            return;
        }
        repositoryService.addModelEditorSource(model.getId(), StrUtil.utf8Bytes(bpmnXml));
    }

    /**
     * 挂起 deploymentId 对应的流程定义
     *
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
        processDefinitionService.updateProcessDefinitionState(oldDefinition.getId(), SuspensionState.SUSPENDED.getStateCode());
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
