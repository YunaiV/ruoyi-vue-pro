package cn.iocoder.yudao.adminserver.modules.bpm.service.definition.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.FileResp;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.processdefinition.ProcessDefinitionPageReqVo;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.processdefinition.ProcessDefinitionRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.convert.workflow.BpmDefinitionConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmProcessDefinitionDO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.definition.BpmProcessDefinitionMapper;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmDefinitionService;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.dto.BpmDefinitionCreateReqDTO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 流程定义实现
 * 主要进行 Activiti {@link ProcessDefinition} 和 {@link Deployment} 的维护
 *
 * @author yunlongn
 */
@Service
@Validated
@Slf4j
public class BpmDefinitionServiceImpl implements BpmDefinitionService {

    private static final String BPMN_FILE_SUFFIX = ".bpmn";

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private BpmProcessDefinitionMapper processDefinitionMapper;

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
                .map(BpmDefinitionConvert.INSTANCE::convert).collect(Collectors.toList());
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

    @Override
    public ProcessDefinition getDefinition(String id) {
        return repositoryService.getProcessDefinition(id);
    }

    @Override
    public List<ProcessDefinition> getDefinitionListByDeploymentIds(Set<String> deploymentIds) {
        if (CollUtil.isEmpty(deploymentIds)) {
            return Collections.emptyList();
        }
        return repositoryService.createProcessDefinitionQuery().deploymentIds(deploymentIds).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 因为进行多个 activiti 操作，所以开启事务
    public String createDefinition(BpmDefinitionCreateReqDTO createReqDTO) {
        // 创建 Deployment 部署
        Deployment deploy = repositoryService.createDeployment()
                .key(createReqDTO.getKey()).name(createReqDTO.getName()).category(createReqDTO.getCategory())
                .addString(createReqDTO.getName() + BPMN_FILE_SUFFIX, createReqDTO.getBpmnXml())
                .deploy();

        // 设置 ProcessDefinition 的 category 分类
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();
        repositoryService.setProcessDefinitionCategory(definition.getId(), createReqDTO.getCategory());

        // 插入拓展表
        BpmProcessDefinitionDO definitionDO = BpmDefinitionConvert.INSTANCE.convert(createReqDTO)
                .setProcessDefinitionId(definition.getId());
        processDefinitionMapper.insert(definitionDO);
        return definition.getId();
    }

}
