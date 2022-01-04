package cn.iocoder.yudao.adminserver.modules.bpm.convert.definition;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.BpmProcessDefinitionPageItemRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmProcessDefinitionDO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.form.BpmFormDO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.dto.BpmDefinitionCreateReqDTO;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * Bpm 流程定义的 Convert
 *
 * @author yunlong.li
 */
@Mapper
public interface BpmDefinitionConvert {

    BpmDefinitionConvert INSTANCE = Mappers.getMapper(BpmDefinitionConvert.class);

    default List<BpmProcessDefinitionPageItemRespVO> convertList(List<ProcessDefinition> list, Map<String, Deployment> deploymentMap,
                               Map<String, BpmProcessDefinitionDO> processDefinitionDOMap, Map<Long, BpmFormDO> formMap) {
        return CollectionUtils.convertList(list, definition -> {
            Deployment deployment = definition.getDeploymentId() != null ? deploymentMap.get(definition.getDeploymentId()) : null;
            BpmProcessDefinitionDO definitionDO = processDefinitionDOMap.get(definition.getId());
            BpmFormDO form = definitionDO != null ? formMap.get(definitionDO.getFormId()) : null;
            return convert(definition, deployment, definitionDO, form);
        });
    }

    default BpmProcessDefinitionPageItemRespVO convert(ProcessDefinition bean, Deployment deployment,
                                                       BpmProcessDefinitionDO processDefinitionDO, BpmFormDO form) {
        BpmProcessDefinitionPageItemRespVO respVO = convert(bean);
        respVO.setSuspensionState(bean.isSuspended() ? SuspensionState.SUSPENDED.getStateCode() : SuspensionState.ACTIVE.getStateCode());
        if (deployment != null) {
            respVO.setDeploymentTime(deployment.getDeploymentTime());
        }
        if (form != null) {
            respVO.setFormId(form.getId());
            respVO.setFormName(form.getName());
        }
        if (processDefinitionDO != null) {
            respVO.setDescription(processDefinitionDO.getDescription());
        }
        return respVO;
    }

    BpmProcessDefinitionPageItemRespVO convert(ProcessDefinition bean);

    BpmProcessDefinitionDO convert2(BpmDefinitionCreateReqDTO bean);

}
