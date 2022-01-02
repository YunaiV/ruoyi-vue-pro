package cn.iocoder.yudao.adminserver.modules.bpm.service.model.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.*;
import cn.iocoder.yudao.adminserver.modules.bpm.convert.model.ModelConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.form.BpmFormDO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmDefinitionService;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.dto.BpmDefinitionCreateReqDTO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.form.BpmFormService;
import cn.iocoder.yudao.adminserver.modules.bpm.service.model.BpmModelService;
import cn.iocoder.yudao.adminserver.modules.bpm.service.model.dto.BpmModelMetaInfoRespDTO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.BPMN_MODEL_NOT_EXISTS;
import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.BPM_MODEL_KEY_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 流程定义实现
 * 主要进行 Activiti {@link Model} 的维护
 *
 * @author yunlongn
 */
@Service
@Validated
@Slf4j
public class BpmModelServiceImpl implements BpmModelService {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private BpmFormService bpmFormService;
    @Resource
    private BpmDefinitionService bpmDefinitionService;

    @Override
    public PageResult<BpmModelPageItemRespVO> getModelPage(ModelPageReqVO pageVO) {
        ModelQuery modelQuery = repositoryService.createModelQuery();
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
        List<Model> models = modelQuery.orderByCreateTime().desc()
                .listPage(PageUtils.getStart(pageVO), pageVO.getPageSize());

        // 获得 Form Map
        Set<Long> formIds = CollectionUtils.convertSet(models, model -> {
            BpmModelMetaInfoRespDTO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoRespDTO.class);
            return metaInfo != null ? metaInfo.getFormId() : null;
        });
        Map<Long, BpmFormDO> formMap = bpmFormService.getFormMap(formIds);

        // 获得 ProcessDefinition Map
        Set<String> deploymentIds = new HashSet<>();
        models.forEach(model -> CollectionUtils.addIfNotNull(deploymentIds, model.getDeploymentId()));
        List<ProcessDefinition> processDefinitions = bpmDefinitionService.getDefinitionListByDeploymentIds(deploymentIds);
        Map<String, ProcessDefinition> processDefinitionMap = convertMap(processDefinitions, ProcessDefinition::getDeploymentId);

        // 拼接结果
        long modelCount = modelQuery.count();
        return new PageResult<>(ModelConvert.INSTANCE.convertList(models, formMap, processDefinitionMap), modelCount);
    }

    @Override
    public BpmModelRespVO getModel(String id) {
        Model model = repositoryService.getModel(id);
        BpmModelRespVO modelRespVO = ModelConvert.INSTANCE.convert(model);
        // 拼接 bpmn XML
        byte[] bpmnBytes = repositoryService.getModelEditorSource(id);
        modelRespVO.setBpmnXml(StrUtil.utf8Str(bpmnBytes));
        return modelRespVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 因为进行多个 activiti 操作，所以开启事务
    public String createModel(BpmModelCreateReqVO createReqVO) {
        // 校验流程标识已经存在
        Model keyModel = this.getModelByKey(createReqVO.getKey());
        if (keyModel != null) {
            throw exception(BPM_MODEL_KEY_EXISTS);
        }
        // TODO @芋艿：需要校验下 key 的格式

        // 创建流程定义
        Model model = repositoryService.newModel();
        ModelConvert.INSTANCE.copy(model, createReqVO);
        // 保存流程定义
        repositoryService.saveModel(model);
        // 添加 BPMN XML
        repositoryService.addModelEditorSource(model.getId(), StrUtil.utf8Bytes(createReqVO.getBpmnXml()));
        return model.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 因为进行多个 activiti 操作，所以开启事务
    public void updateModel(BpmModelUpdateReqVO updateReqVO) {
        // 校验流程模型存在
        Model model = repositoryService.getModel(updateReqVO.getId());
        if (model == null) {
            throw exception(BPMN_MODEL_NOT_EXISTS);
        }
        // TODO @芋艿：需要校验下 key 的格式

        // 修改流程定义
        ModelConvert.INSTANCE.copy(model, updateReqVO);
        // 更新模型
        repositoryService.saveModel(model);
        // 更新 BPMN XML
        repositoryService.addModelEditorSource(model.getId(), StrUtil.utf8Bytes(updateReqVO.getBpmnXml()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 因为进行多个 activiti 操作，所以开启事务
    public void deployModel(String id) {
        // 校验流程模型存在
        Model model = repositoryService.getModel(id);
        if (ObjectUtils.isEmpty(model)) {
            throw exception(BPMN_MODEL_NOT_EXISTS);
        }
        byte[] bpmnBytes = repositoryService.getModelEditorSource(model.getId());
        if (bpmnBytes == null) {
            throw exception(BPMN_MODEL_NOT_EXISTS);
        }

        // 创建流程定义
        BpmDefinitionCreateReqDTO definitionCreateReqDTO = ModelConvert.INSTANCE.convert2(model)
                .setBpmnXml(StrUtil.utf8Str(bpmnBytes));
        String definitionId = bpmDefinitionService.createDefinition(definitionCreateReqDTO);

        // 更新 model 的 deploymentId，进行关联
        ProcessDefinition definition = bpmDefinitionService.getDefinition(definitionId);
        model.setDeploymentId(definition.getDeploymentId());
        repositoryService.saveModel(model);
    }

    @Override
    public void deleteModel(String id) {
        // 校验流程模型存在
        Model model = repositoryService.getModel(id);
        if (model == null) {
            throw exception(BPMN_MODEL_NOT_EXISTS);
        }
        // 执行删除
        repositoryService.deleteModel(id);
    }

    private Model getModelByKey(String key) {
        return repositoryService.createModelQuery().modelKey(key).singleResult();
    }

}
