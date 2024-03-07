package cn.iocoder.yudao.module.bpm.convert.definition;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.*;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.service.definition.dto.BpmModelMetaInfoRespDTO;
import cn.iocoder.yudao.module.bpm.service.definition.dto.BpmProcessDefinitionCreateReqDTO;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 流程模型 Convert
 *
 * @author yunlongn
 */
@Mapper
public interface BpmModelConvert {

    BpmModelConvert INSTANCE = Mappers.getMapper(BpmModelConvert.class);

    default List<BpmModelPageItemRespVO> convertList(List<Model> list, Map<Long, BpmFormDO> formMap,
                                                     Map<String, Deployment> deploymentMap,
                                                     Map<String, ProcessDefinition> processDefinitionMap) {
        return CollectionUtils.convertList(list, model -> {
            BpmModelMetaInfoRespDTO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoRespDTO.class);
            BpmFormDO form = metaInfo != null ? formMap.get(metaInfo.getFormId()) : null;
            Deployment deployment = model.getDeploymentId() != null ? deploymentMap.get(model.getDeploymentId()) : null;
            ProcessDefinition processDefinition = model.getDeploymentId() != null ? processDefinitionMap.get(model.getDeploymentId()) : null;
            return convert(model, form, deployment, processDefinition);
        });
    }

    default BpmModelPageItemRespVO convert(Model model, BpmFormDO form, Deployment deployment, ProcessDefinition processDefinition) {
        BpmModelPageItemRespVO modelRespVO = new BpmModelPageItemRespVO();
        modelRespVO.setId(model.getId());
        modelRespVO.setCreateTime(DateUtils.of(model.getCreateTime()));
        // 通用 copy
        copyTo(model, modelRespVO);
        // Form
        if (form != null) {
            modelRespVO.setFormId(form.getId());
            modelRespVO.setFormName(form.getName());
        }
        // ProcessDefinition
        modelRespVO.setProcessDefinition(this.convert(processDefinition));
        if (modelRespVO.getProcessDefinition() != null) {
            modelRespVO.getProcessDefinition().setSuspensionState(processDefinition.isSuspended() ?
                    SuspensionState.SUSPENDED.getStateCode() : SuspensionState.ACTIVE.getStateCode());
            modelRespVO.getProcessDefinition().setDeploymentTime(DateUtils.of(deployment.getDeploymentTime()));
        }
        return modelRespVO;
    }

    default BpmModelRespVO convert(Model model) {
        BpmModelRespVO modelRespVO = new BpmModelRespVO();
        modelRespVO.setId(model.getId());
        modelRespVO.setCreateTime(DateUtils.of(model.getCreateTime()));
        // 通用 copy
        copyTo(model, modelRespVO);
        return modelRespVO;
    }

    default void copyTo(Model model, BpmModelBaseVO to) {
        to.setName(model.getName());
        to.setKey(model.getKey());
        to.setCategory(model.getCategory());
        // metaInfo
        BpmModelMetaInfoRespDTO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoRespDTO.class);
        copyTo(metaInfo, to);
    }

    BpmModelCreateReqVO convert(BpmModeImportReqVO bean);

    default BpmProcessDefinitionCreateReqDTO convert2(Model model, BpmFormDO form) {
        BpmProcessDefinitionCreateReqDTO createReqDTO = new BpmProcessDefinitionCreateReqDTO();
        createReqDTO.setModelId(model.getId());
        createReqDTO.setName(model.getName());
        createReqDTO.setKey(model.getKey());
        createReqDTO.setCategory(model.getCategory());
        BpmModelMetaInfoRespDTO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoRespDTO.class);
        // metaInfo
        copyTo(metaInfo, createReqDTO);
        // form
        if (form != null) {
            createReqDTO.setFormConf(form.getConf());
            createReqDTO.setFormFields(form.getFields());
        }
        return createReqDTO;
    }

    void copyTo(BpmModelMetaInfoRespDTO from, @MappingTarget BpmProcessDefinitionCreateReqDTO to);

    void copyTo(BpmModelMetaInfoRespDTO from, @MappingTarget BpmModelBaseVO to);

    BpmModelPageItemRespVO.ProcessDefinition convert(ProcessDefinition bean);

    default void copy(Model model, BpmModelCreateReqVO bean) {
        model.setName(bean.getName());
        model.setKey(bean.getKey());
        model.setMetaInfo(buildMetaInfoStr(null, bean.getDescription(), null, null,
                null, null));
    }

    default void copy(Model model, BpmModelUpdateReqVO bean) {
        model.setName(bean.getName());
        model.setCategory(bean.getCategory());
        model.setMetaInfo(buildMetaInfoStr(JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoRespDTO.class),
                bean.getDescription(), bean.getFormType(), bean.getFormId(),
                bean.getFormCustomCreatePath(), bean.getFormCustomViewPath()));
    }

    default String buildMetaInfoStr(BpmModelMetaInfoRespDTO metaInfo, String description, Integer formType,
                                    Long formId, String formCustomCreatePath, String formCustomViewPath) {
        if (metaInfo == null) {
            metaInfo = new BpmModelMetaInfoRespDTO();
        }
        // 只有非空，才进行设置，避免更新时的覆盖
        if (StrUtil.isNotEmpty(description)) {
            metaInfo.setDescription(description);
        }
        if (Objects.nonNull(formType)) {
            metaInfo.setFormType(formType);
            metaInfo.setFormId(formId);
            metaInfo.setFormCustomCreatePath(formCustomCreatePath);
            metaInfo.setFormCustomViewPath(formCustomViewPath);
        }
        return JsonUtils.toJsonString(metaInfo);
    }
}
