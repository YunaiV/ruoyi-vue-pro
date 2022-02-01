package cn.iocoder.yudao.module.bpm.convert.task;

import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstancePageItemRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceRespVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionExtDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmProcessInstanceExtDO;
import cn.iocoder.yudao.module.bpm.framework.bpm.core.event.BpmProcessInstanceResultEvent;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceApproveReqDTO;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceRejectReqDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * 流程实例 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface BpmProcessInstanceConvert {

    BpmProcessInstanceConvert INSTANCE = Mappers.getMapper(BpmProcessInstanceConvert.class);

    default BpmProcessInstanceExtDO convert3(ProcessInstance instance, ProcessDefinition definition) {
        BpmProcessInstanceExtDO ext = new BpmProcessInstanceExtDO();
        copyTo(instance, ext);
        copyTo(definition, ext);
        return ext;
    }

    @Mappings({
            @Mapping(source = "from.id", target = "id", ignore = true),
            @Mapping(source = "from.startTime", target = "createTime"),
    })
    void copyTo(ProcessInstance from, @MappingTarget BpmProcessInstanceExtDO to);
    @Mapping(source = "from.id", target = "id", ignore = true)
    void copyTo(ProcessDefinition from, @MappingTarget BpmProcessInstanceExtDO to);

    default PageResult<BpmProcessInstancePageItemRespVO> convertPage(PageResult<BpmProcessInstanceExtDO> page,
                                                                     Map<String, List<Task>> taskMap) {
        List<BpmProcessInstancePageItemRespVO> list = convertList(page.getList());
        list.forEach(respVO -> respVO.setTasks(convertList2(taskMap.get(respVO.getId()))));
        return new PageResult<>(list, page.getTotal());
    }

    List<BpmProcessInstancePageItemRespVO> convertList(List<BpmProcessInstanceExtDO> list);

    List<BpmProcessInstancePageItemRespVO.Task> convertList2(List<Task> tasks);

    @Mapping(source = "processInstanceId", target = "id")
    BpmProcessInstancePageItemRespVO convert(BpmProcessInstanceExtDO bean);

    @Mappings({
            @Mapping(source = "id", target = "processInstanceId"),
            @Mapping(source = "id", target = "id", ignore = true),
            @Mapping(source = "startDate", target = "createTime"),
            @Mapping(source = "initiator", target = "startUserId"),
            @Mapping(source = "status", target = "status", ignore = true)
    })
    BpmProcessInstanceExtDO convert(org.activiti.api.process.model.ProcessInstance bean);

    default BpmProcessInstanceRespVO convert2(HistoricProcessInstance processInstance, BpmProcessInstanceExtDO processInstanceExt,
                                              ProcessDefinition processDefinition, BpmProcessDefinitionExtDO processDefinitionExt,
                                              String bpmnXml, AdminUserRespDTO startUser, DeptRespDTO dept) {
        BpmProcessInstanceRespVO respVO = convert2(processInstance);
        copyTo(processInstanceExt, respVO);
        // definition
        respVO.setProcessDefinition(convert2(processDefinition));
        copyTo(processDefinitionExt, respVO.getProcessDefinition());
        respVO.getProcessDefinition().setBpmnXml(bpmnXml);
        // user
        if (startUser != null) {
            respVO.setStartUser(convert2(startUser));
            if (dept != null) {
                respVO.getStartUser().setDeptName(dept.getName());
            }
        }
        return respVO;
    }

    BpmProcessInstanceRespVO convert2(HistoricProcessInstance bean);
    @Mapping(source = "from.id", target = "to.id", ignore = true)
    void copyTo(BpmProcessInstanceExtDO from, @MappingTarget BpmProcessInstanceRespVO to);
    BpmProcessInstanceRespVO.ProcessDefinition convert2(ProcessDefinition bean);
    @Mapping(source = "from.id", target = "to.id", ignore = true)
    void copyTo(BpmProcessDefinitionExtDO from, @MappingTarget BpmProcessInstanceRespVO.ProcessDefinition to);
    BpmProcessInstanceRespVO.User convert2(AdminUserRespDTO bean);

    default BpmProcessInstanceResultEvent convert(Object source, ProcessInstance instance, Integer result) {
        BpmProcessInstanceResultEvent event = new BpmProcessInstanceResultEvent(source);
        event.setId(instance.getId());
        event.setProcessDefinitionKey(instance.getProcessDefinitionKey());
        event.setBusinessKey(instance.getBusinessKey());
        event.setResult(result);
        return event;
    }

    default BpmProcessInstanceResultEvent convert(Object source, HistoricProcessInstance instance, Integer result) {
        BpmProcessInstanceResultEvent event = new BpmProcessInstanceResultEvent(source);
        event.setId(instance.getId());
        event.setProcessDefinitionKey(instance.getProcessDefinitionKey());
        event.setBusinessKey(instance.getBusinessKey());
        event.setResult(result);
        return event;
    }

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
    BpmMessageSendWhenProcessInstanceApproveReqDTO convert2(org.activiti.api.process.model.ProcessInstance processInstance);

}
