package cn.iocoder.yudao.adminserver.modules.bpm.convert.task;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.instance.BpmProcessInstancePageItemRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.task.BpmProcessInstanceExtDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 流程实例 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface BpmProcessInstanceConvert {

    BpmProcessInstanceConvert INSTANCE = Mappers.getMapper(BpmProcessInstanceConvert.class);

    @Mappings({
            @Mapping(source = "instance.startUserId", target = "userId"),
            @Mapping(source = "instance.id", target = "processInstanceId"),
            @Mapping(source = "instance.startTime", target = "createTime"),
            @Mapping(source = "definition.id", target = "processDefinitionId"),
            @Mapping(source = "definition.name", target = "name"),
            @Mapping(source = "definition.category", target = "category")
    })
    BpmProcessInstanceExtDO convert(ProcessInstance instance, ProcessDefinition definition);

    PageResult<BpmProcessInstancePageItemRespVO> convertPage(PageResult<BpmProcessInstanceExtDO> page);

    List<BpmProcessInstancePageItemRespVO> convertList(List<BpmProcessInstanceExtDO> list);

    @Mapping(source = "processInstanceId", target = "id")
    BpmProcessInstancePageItemRespVO convert(BpmProcessInstanceExtDO bean);

}
