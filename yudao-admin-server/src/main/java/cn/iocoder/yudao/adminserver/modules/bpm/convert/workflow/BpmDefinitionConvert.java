package cn.iocoder.yudao.adminserver.modules.bpm.convert.workflow;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.processdefinition.ProcessDefinitionRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmProcessDefinitionDO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.dto.BpmDefinitionCreateReqDTO;
import org.activiti.engine.repository.ProcessDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Bpm 流程定义的 Convert
 *
 * @author yunlong.li
 */
@Mapper
public interface BpmDefinitionConvert {

    BpmDefinitionConvert INSTANCE = Mappers.getMapper(BpmDefinitionConvert.class);

    ProcessDefinitionRespVO convert(ProcessDefinition processDefinition);

    BpmProcessDefinitionDO convert(BpmDefinitionCreateReqDTO bean);

}
