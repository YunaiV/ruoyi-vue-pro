package cn.iocoder.yudao.module.bpm.convert.definition;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionPageItemRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionRespVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionExtDO;
import cn.iocoder.yudao.module.bpm.service.definition.dto.BpmProcessDefinitionCreateReqDTO;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * Bpm 流程定义的 Convert
 *
 * @author yunlong.li
 */
@Mapper
public interface BpmProcessDefinitionConvert {

    BpmProcessDefinitionConvert INSTANCE = Mappers.getMapper(BpmProcessDefinitionConvert.class);

    BpmProcessDefinitionPageItemRespVO convert(ProcessDefinition bean);

    BpmProcessDefinitionExtDO convert2(BpmProcessDefinitionCreateReqDTO bean);

    default List<BpmProcessDefinitionPageItemRespVO> convertList(List<ProcessDefinition> list, Map<String, Deployment> deploymentMap,
                                                                 Map<String, BpmProcessDefinitionExtDO> processDefinitionDOMap, Map<Long, BpmFormDO> formMap) {
        return CollectionUtils.convertList(list, definition -> {
            Deployment deployment = definition.getDeploymentId() != null ? deploymentMap.get(definition.getDeploymentId()) : null;
            BpmProcessDefinitionExtDO definitionDO = processDefinitionDOMap.get(definition.getId());
            BpmFormDO form = definitionDO != null ? formMap.get(definitionDO.getFormId()) : null;
            return convert(definition, deployment, definitionDO, form);
        });
    }

    default List<BpmProcessDefinitionRespVO> convertList3(List<ProcessDefinition> list,
                                                          Map<String, BpmProcessDefinitionExtDO> processDefinitionDOMap) {
        return CollectionUtils.convertList(list, processDefinition -> {
            BpmProcessDefinitionRespVO respVO = convert3(processDefinition);
            BpmProcessDefinitionExtDO processDefinitionExtDO = processDefinitionDOMap.get(processDefinition.getId());
            // 复制通用属性
            copyTo(processDefinitionExtDO, respVO);
            return respVO;
        });
    }

    @Mapping(source = "suspended", target = "suspensionState", qualifiedByName = "convertSuspendedToSuspensionState")
    BpmProcessDefinitionRespVO convert3(ProcessDefinition bean);

    @Named("convertSuspendedToSuspensionState")
    default Integer convertSuspendedToSuspensionState(boolean suspended) {
        return suspended ? SuspensionState.SUSPENDED.getStateCode() :
                SuspensionState.ACTIVE.getStateCode();
    }

    default BpmProcessDefinitionPageItemRespVO convert(ProcessDefinition bean, Deployment deployment,
                                                       BpmProcessDefinitionExtDO processDefinitionExtDO, BpmFormDO form) {
        BpmProcessDefinitionPageItemRespVO respVO = convert(bean);
        respVO.setSuspensionState(bean.isSuspended() ? SuspensionState.SUSPENDED.getStateCode() : SuspensionState.ACTIVE.getStateCode());
        if (deployment != null) {
            respVO.setDeploymentTime(deployment.getDeploymentTime());
        }
        if (form != null) {
            respVO.setFormName(form.getName());
        }
        // 复制通用属性
        copyTo(processDefinitionExtDO, respVO);
        return respVO;
    }

    @Mapping(source = "from.id", target = "to.id", ignore = true)
    void copyTo(BpmProcessDefinitionExtDO from, @MappingTarget BpmProcessDefinitionRespVO to);
}
