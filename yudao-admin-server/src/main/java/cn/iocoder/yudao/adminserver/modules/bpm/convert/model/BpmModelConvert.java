package cn.iocoder.yudao.adminserver.modules.bpm.convert.model;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.*;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.form.BpmFormDO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.dto.BpmDefinitionCreateReqDTO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.dto.BpmModelMetaInfoRespDTO;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;


/**
 * 流程定义 Convert
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
        modelRespVO.setName(model.getName());
        modelRespVO.setKey(model.getKey());
        modelRespVO.setCategory(model.getCategory());
        modelRespVO.setCreateTime(model.getCreateTime());
        BpmModelMetaInfoRespDTO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoRespDTO.class);
        if (metaInfo != null) {
            modelRespVO.setDescription(metaInfo.getDescription());
        }
        if (form != null) {
            modelRespVO.setFormId(form.getId());
            modelRespVO.setFormName(form.getName());
        }
        modelRespVO.setProcessDefinition(this.convert(processDefinition));
        if (modelRespVO.getProcessDefinition() != null) {
            modelRespVO.getProcessDefinition().setSuspensionState(processDefinition.isSuspended() ?
                    SuspensionState.SUSPENDED.getStateCode() : SuspensionState.ACTIVE.getStateCode());
            modelRespVO.getProcessDefinition().setDeploymentTime(deployment.getDeploymentTime());
        }
        return modelRespVO;
    }

    default BpmModelRespVO convert(Model model) {
        BpmModelRespVO modelRespVO = new BpmModelRespVO();
        modelRespVO.setId(model.getId());
        modelRespVO.setName(model.getName());
        modelRespVO.setKey(model.getKey());
        modelRespVO.setCategory(model.getCategory());
        modelRespVO.setCreateTime(model.getCreateTime());
        BpmModelMetaInfoRespDTO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoRespDTO.class);
        if (metaInfo != null) {
            modelRespVO.setFormId(metaInfo.getFormId());
            modelRespVO.setDescription(metaInfo.getDescription());
        }
        return modelRespVO;
    }

    default BpmDefinitionCreateReqDTO convert2(Model model) {
        BpmDefinitionCreateReqDTO createReqDTO = new BpmDefinitionCreateReqDTO();
        createReqDTO.setModelId(model.getId());
        createReqDTO.setName(model.getName());
        createReqDTO.setKey(model.getKey());
        createReqDTO.setCategory(model.getCategory());
        BpmModelMetaInfoRespDTO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoRespDTO.class);
        if (metaInfo != null) {
            createReqDTO.setDescription(metaInfo.getDescription());
            createReqDTO.setFormId(metaInfo.getFormId());
        }
        return createReqDTO;
    }

    default void copy(Model model, BpmModelCreateReqVO bean) {
        model.setName(bean.getName());
        model.setKey(bean.getKey());
        model.setMetaInfo(JsonUtils.toJsonString(this.buildMetaInfo(bean.getDescription(), null)));
    }

    default void copy(Model model, BpmModelUpdateReqVO bean) {
        model.setName(bean.getName());
        model.setCategory(bean.getCategory());
        model.setMetaInfo(JsonUtils.toJsonString(this.buildMetaInfo(bean.getDescription(), bean.getFormId())));
    }

    default BpmModelMetaInfoRespDTO buildMetaInfo(String description, Long formId) {
        BpmModelMetaInfoRespDTO metaInfo = new BpmModelMetaInfoRespDTO();
        metaInfo.setDescription(description);
        metaInfo.setFormId(formId);
        return metaInfo;
    }

    BpmModelPageItemRespVO.ProcessDefinition convert(ProcessDefinition bean);

    BpmModelCreateReqVO convert(BpmModeImportReqVO bean);

}
