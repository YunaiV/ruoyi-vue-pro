package cn.iocoder.yudao.adminserver.modules.bpm.convert.workflow;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.processdefinition.ProcessDefinitionRespVO;
import org.activiti.engine.repository.ProcessDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author yunlong.li
 */
@Mapper
public interface ProcessDefinitionConvert {
    ProcessDefinitionConvert INSTANCE = Mappers.getMapper(ProcessDefinitionConvert.class);

    ProcessDefinitionRespVO convert(ProcessDefinition processDefinition);

}
