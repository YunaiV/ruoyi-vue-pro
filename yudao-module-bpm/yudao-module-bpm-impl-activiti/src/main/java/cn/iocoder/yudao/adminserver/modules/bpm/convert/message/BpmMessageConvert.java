package cn.iocoder.yudao.adminserver.modules.bpm.convert.message;

import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceApproveReqDTO;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceRejectReqDTO;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenTaskCreatedReqDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.SysUserDO;
import org.activiti.api.task.model.Task;
import org.activiti.engine.runtime.ProcessInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BpmMessageConvert {

    BpmMessageConvert INSTANCE = Mappers.getMapper(BpmMessageConvert.class);

    default BpmMessageSendWhenTaskCreatedReqDTO convert(ProcessInstance processInstance, SysUserDO startUser, Task task) {
        BpmMessageSendWhenTaskCreatedReqDTO reqDTO = new BpmMessageSendWhenTaskCreatedReqDTO();
        copyTo(processInstance, reqDTO);
        copyTo(startUser, reqDTO);
        copyTo(task, reqDTO);
        return reqDTO;
    }
    @Mapping(source = "name", target = "processInstanceName")
    void copyTo(ProcessInstance from, @MappingTarget BpmMessageSendWhenTaskCreatedReqDTO to);
    @Mappings({
            @Mapping(source = "id", target = "startUserId"),
            @Mapping(source = "nickname", target = "startUserNickname")
    })
    void copyTo(SysUserDO from, @MappingTarget BpmMessageSendWhenTaskCreatedReqDTO to);
    @Mappings({
            @Mapping(source = "id", target = "taskId"),
            @Mapping(source = "name", target = "taskName"),
            @Mapping(source = "assignee", target = "assigneeUserId")
    })
    void copyTo(Task task, @MappingTarget BpmMessageSendWhenTaskCreatedReqDTO to);

    default BpmMessageSendWhenProcessInstanceRejectReqDTO convert(ProcessInstance processInstance, String comment) {
        BpmMessageSendWhenProcessInstanceRejectReqDTO reqDTO = new BpmMessageSendWhenProcessInstanceRejectReqDTO();
        copyTo(processInstance, reqDTO);
        reqDTO.setComment(comment);
        return reqDTO;
    }
    @Mapping(source = "name", target = "processInstanceName")
    void copyTo(ProcessInstance from, @MappingTarget BpmMessageSendWhenProcessInstanceRejectReqDTO to);

    @Mappings({
            @Mapping(source = "id", target = "processInstanceId"),
            @Mapping(source = "name", target = "processInstanceName"),
            @Mapping(source = "initiator", target = "startUserId")
    })
    BpmMessageSendWhenProcessInstanceApproveReqDTO convert(org.activiti.api.process.model.ProcessInstance processInstance);

}
