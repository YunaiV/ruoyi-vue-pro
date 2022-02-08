package cn.iocoder.yudao.module.bpm.convert.definition;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelBaseVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelPageItemRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelRespVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.service.definition.dto.ModelMetaInfoRespDTO;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * 流程模型 Convert
 *
 * @author yunlongn
 */
@Mapper
public interface ModelConvert {

    ModelConvert INSTANCE = Mappers.getMapper(ModelConvert.class);

    default List<BpmModelPageItemRespVO> convertList(List<Model> list, Map<Long, BpmFormDO> formMap,
                                                     Map<String, Deployment> deploymentMap,
                                                     Map<String, ProcessDefinition> processDefinitionMap) {
        return CollectionUtils.convertList(list, model -> {
            ModelMetaInfoRespDTO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), ModelMetaInfoRespDTO.class);
            BpmFormDO form = metaInfo != null ? formMap.get(metaInfo.getFormId()) : null;
            Deployment deployment = model.getDeploymentId() != null ? deploymentMap.get(model.getDeploymentId()) : null;
            ProcessDefinition processDefinition = model.getDeploymentId() != null ? processDefinitionMap.get(model.getDeploymentId()) : null;
            return convert(model, form, deployment, processDefinition);
        });
    }

    default BpmModelPageItemRespVO convert(Model model, BpmFormDO form, Deployment deployment, ProcessDefinition processDefinition) {
        BpmModelPageItemRespVO modelRespVO = new BpmModelPageItemRespVO();
        modelRespVO.setId(model.getId());
        modelRespVO.setCreateTime(model.getCreateTime());
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
            modelRespVO.getProcessDefinition().setDeploymentTime(deployment.getDeploymentTime());
        }
        return modelRespVO;
    }

    default BpmModelRespVO convert(Model model) {
        BpmModelRespVO modelRespVO = new BpmModelRespVO();
        modelRespVO.setId(model.getId());
        modelRespVO.setCreateTime(model.getCreateTime());
        // 通用 copy
        copyTo(model, modelRespVO);
        return modelRespVO;
    }

    default void copyTo(Model model, BpmModelBaseVO to) {
        to.setName(model.getName());
        to.setKey(model.getKey());
        to.setCategory(model.getCategory());
        // metaInfo
        ModelMetaInfoRespDTO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), ModelMetaInfoRespDTO.class);
        copyTo(metaInfo, to);
    }

    void copyTo(ModelMetaInfoRespDTO from, @MappingTarget BpmModelBaseVO to);

    BpmModelPageItemRespVO.ProcessDefinition convert(ProcessDefinition bean);
}
