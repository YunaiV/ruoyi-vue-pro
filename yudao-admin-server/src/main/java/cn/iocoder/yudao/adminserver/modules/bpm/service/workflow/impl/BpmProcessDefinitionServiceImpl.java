package cn.iocoder.yudao.adminserver.modules.bpm.service.workflow.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.FileResp;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.processdefinition.ProcessDefinitionPageReqVo;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.processdefinition.ProcessDefinitionRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.convert.workflow.ProcessDefinitionConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.service.workflow.BpmProcessDefinitionService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程定义实现
 * @author yunlongn
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BpmProcessDefinitionServiceImpl implements BpmProcessDefinitionService {

    private final RepositoryService repositoryService;

    private final ProcessRuntime processRuntime;

    @Override
    public PageResult<ProcessDefinitionRespVO> pageList(ProcessDefinitionPageReqVo processDefinitionPageReqVo) {
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        String likeName = processDefinitionPageReqVo.getName();
        if (StrUtil.isNotBlank(likeName)){
            processDefinitionQuery.processDefinitionNameLike("%"+likeName+"%");
        }
        List<ProcessDefinition> processDefinitions = processDefinitionQuery.orderByProcessDefinitionId().desc()
                .listPage((processDefinitionPageReqVo.getPageNo() - 1) * processDefinitionPageReqVo.getPageSize(),
                        processDefinitionPageReqVo.getPageSize());
        final List<ProcessDefinitionRespVO> respVOList = processDefinitions.stream()
                .map(ProcessDefinitionConvert.INSTANCE::convert).collect(Collectors.toList());
        return new PageResult<>(respVOList, processDefinitionQuery.count());
    }

    @Override
    public FileResp export(String processDefinitionId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
        FileResp fileResp = new FileResp();
        fileResp.setFileName( "export");
        fileResp.setFileByte(bpmnBytes);
        return fileResp;
    }
}
