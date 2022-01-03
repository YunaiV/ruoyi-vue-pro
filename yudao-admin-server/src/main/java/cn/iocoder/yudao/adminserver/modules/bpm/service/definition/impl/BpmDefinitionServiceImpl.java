package cn.iocoder.yudao.adminserver.modules.bpm.service.definition.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.BpmProcessDefinitionPageItemRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.BpmProcessDefinitionPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.FileResp;
import cn.iocoder.yudao.adminserver.modules.bpm.convert.definition.BpmDefinitionConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmProcessDefinitionDO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.form.BpmFormDO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.definition.BpmProcessDefinitionMapper;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmDefinitionService;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.dto.BpmDefinitionCreateReqDTO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.form.BpmFormService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
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
import java.util.*;

import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.DEFINITION_KEY_NOT_MATCH;
import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.DEFINITION_NAME_NOT_MATCH;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 流程定义实现
 * 主要进行 Activiti {@link ProcessDefinition} 和 {@link Deployment} 的维护
 *
 * @author yunlongn
 * @author ZJQ
 */
@Service
@Validated
@Slf4j
public class BpmDefinitionServiceImpl implements BpmDefinitionService {

    private static final String BPMN_FILE_SUFFIX = ".bpmn";

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private BpmFormService bpmFormService;

    @Resource
    private BpmProcessDefinitionMapper processDefinitionMapper;

    @Override
    public PageResult<BpmProcessDefinitionPageItemRespVO> getDefinitionPage(BpmProcessDefinitionPageReqVO pageVO) {
        ProcessDefinitionQuery definitionQuery = repositoryService.createProcessDefinitionQuery();
        if (StrUtil.isNotBlank(pageVO.getKey())) {
            definitionQuery.processDefinitionKey(pageVO.getKey());
        }
        // 执行查询
        List<ProcessDefinition> processDefinitions = definitionQuery.orderByProcessDefinitionId().desc()
                .listPage(PageUtils.getStart(pageVO), pageVO.getPageSize());
        if (CollUtil.isEmpty(processDefinitions)) {
            return new PageResult<>(Collections.emptyList(), definitionQuery.count());
        }

        // 获得 Deployment Map
        Set<String> deploymentIds = new HashSet<>();
        processDefinitions.forEach(definition -> CollectionUtils.addIfNotNull(deploymentIds, definition.getDeploymentId()));
        Map<String, Deployment> deploymentMap = getDeploymentMap(deploymentIds);

        // 获得 BpmProcessDefinitionDO Map
        List<BpmProcessDefinitionDO> processDefinitionDOs = Collections.emptyList();
        processDefinitionDOs = processDefinitionMapper.selectListByProcessDefinitionIds(
                convertList(processDefinitions, ProcessDefinition::getId));
        Map<String, BpmProcessDefinitionDO> processDefinitionDOMap = CollectionUtils.convertMap(processDefinitionDOs,
                BpmProcessDefinitionDO::getProcessDefinitionId);

        // 获得 Form Map
        Set<Long> formIds = CollectionUtils.convertSet(processDefinitionDOs, BpmProcessDefinitionDO::getFormId);
        Map<Long, BpmFormDO> formMap = bpmFormService.getFormMap(formIds);

        // 拼接结果
        long definitionCount = definitionQuery.count();
        return new PageResult<>(BpmDefinitionConvert.INSTANCE.convertList(processDefinitions, deploymentMap,
                processDefinitionDOMap, formMap), definitionCount);
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
    public Deployment getDeployment(String id) {
        if (StrUtil.isEmpty(id)) {
            return null;
        }
        return repositoryService.createDeploymentQuery().deploymentId(id).singleResult();
    }

    @Override
    public List<Deployment> getDeployments(Set<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<Deployment> list = new ArrayList<>(ids.size());
        for (String id : ids) {
            CollectionUtils.addIfNotNull(list, getDeployment(id));
        }
        return list;
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
        // 注意 1，ProcessDefinition 的 key 和 name 是通过 BPMN 中的 <bpmn2:process /> 的 id 和 name 决定
        // 注意 2，目前该项目的设计上，需要保证 Model、Deployment、ProcessDefinition 使用相同的 key，保证关联性。
        //          否则，会导致 ProcessDefinition 的分页无法查询到。
        if (!Objects.equals(definition.getKey(), createReqDTO.getKey())) {
            throw exception(DEFINITION_KEY_NOT_MATCH, createReqDTO.getKey(), definition.getKey());
        }
        if (!Objects.equals(definition.getName(), createReqDTO.getName())) {
            throw exception(DEFINITION_NAME_NOT_MATCH, createReqDTO.getName(), definition.getName());
        }

        // 插入拓展表
        BpmProcessDefinitionDO definitionDO = BpmDefinitionConvert.INSTANCE.convert2(createReqDTO)
                .setProcessDefinitionId(definition.getId());
        processDefinitionMapper.insert(definitionDO);
        return definition.getId();
    }

}
