package cn.iocoder.yudao.adminserver.modules.bpm.service.definition.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.BpmProcessDefinitionListReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.BpmProcessDefinitionPageItemRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.BpmProcessDefinitionPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.BpmProcessDefinitionRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.convert.definition.BpmDefinitionConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmProcessDefinitionExtDO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.form.BpmFormDO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.definition.BpmProcessDefinitionExtMapper;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.dto.BpmDefinitionCreateReqDTO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.form.BpmFormService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.PROCESS_DEFINITION_KEY_NOT_MATCH;
import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.PROCESS_DEFINITION_NAME_NOT_MATCH;
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
public class BpmProcessDefinitionServiceImpl implements BpmProcessDefinitionService {

    private static final String BPMN_FILE_SUFFIX = ".bpmn";

    private static final BpmnXMLConverter BPMN_XML_CONVERTER = new BpmnXMLConverter();

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private BpmFormService bpmFormService;

    @Resource
    private BpmProcessDefinitionExtMapper processDefinitionMapper;

    @Override
    public PageResult<BpmProcessDefinitionPageItemRespVO> getProcessDefinitionPage(BpmProcessDefinitionPageReqVO pageVO) {
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
        List<BpmProcessDefinitionExtDO> processDefinitionDOs = processDefinitionMapper.selectListByProcessDefinitionIds(
                convertList(processDefinitions, ProcessDefinition::getId));
        Map<String, BpmProcessDefinitionExtDO> processDefinitionDOMap = CollectionUtils.convertMap(processDefinitionDOs,
                BpmProcessDefinitionExtDO::getProcessDefinitionId);

        // 获得 Form Map
        Set<Long> formIds = CollectionUtils.convertSet(processDefinitionDOs, BpmProcessDefinitionExtDO::getFormId);
        Map<Long, BpmFormDO> formMap = bpmFormService.getFormMap(formIds);

        // 拼接结果
        long definitionCount = definitionQuery.count();
        return new PageResult<>(BpmDefinitionConvert.INSTANCE.convertList(processDefinitions, deploymentMap,
                processDefinitionDOMap, formMap), definitionCount);
    }

    @Override
    public List<BpmProcessDefinitionRespVO> getProcessDefinitionList(BpmProcessDefinitionListReqVO listReqVO) {
        // 拼接查询条件
        ProcessDefinitionQuery definitionQuery = repositoryService.createProcessDefinitionQuery();
        if (Objects.equals(SuspensionState.SUSPENDED.getStateCode(), listReqVO.getSuspensionState())) {
            definitionQuery.suspended();
        } else if (Objects.equals(SuspensionState.ACTIVE.getStateCode(), listReqVO.getSuspensionState())) {
            definitionQuery.active();
        }
        // 执行查询
        List<ProcessDefinition> processDefinitions = definitionQuery.list();

        // 获得 BpmProcessDefinitionDO Map
        List<BpmProcessDefinitionExtDO> processDefinitionDOs = processDefinitionMapper.selectListByProcessDefinitionIds(
                convertList(processDefinitions, ProcessDefinition::getId));
        Map<String, BpmProcessDefinitionExtDO> processDefinitionDOMap = CollectionUtils.convertMap(processDefinitionDOs,
                BpmProcessDefinitionExtDO::getProcessDefinitionId);
        // 执行查询，并返回
        return BpmDefinitionConvert.INSTANCE.convertList3(processDefinitions, processDefinitionDOMap);
    }

    @Override
    public String getProcessDefinitionBpmnXML(String id) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(id);
        if (bpmnModel == null) {
            return null;
        }
        byte[] bpmnBytes = BPMN_XML_CONVERTER.convertToXML(bpmnModel);
        return StrUtil.utf8Str(bpmnBytes);
    }

    @Override
    public BpmnModel getBpmnModel(String processDefinitionId) {
        return repositoryService.getBpmnModel(processDefinitionId);
    }

    @Override
    public ProcessDefinition getProcessDefinition(String id) {
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
    public ProcessDefinition getProcessDefinitionByDeploymentId(String deploymentId) {
        return repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
    }

    @Override
    public List<ProcessDefinition> getProcessDefinitionListByDeploymentIds(Set<String> deploymentIds) {
        if (CollUtil.isEmpty(deploymentIds)) {
            return Collections.emptyList();
        }
        return repositoryService.createProcessDefinitionQuery().deploymentIds(deploymentIds).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 因为进行多个 activiti 操作，所以开启事务
    public String createProcessDefinition(BpmDefinitionCreateReqDTO createReqDTO) {
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
            throw exception(PROCESS_DEFINITION_KEY_NOT_MATCH, createReqDTO.getKey(), definition.getKey());
        }
        if (!Objects.equals(definition.getName(), createReqDTO.getName())) {
            throw exception(PROCESS_DEFINITION_NAME_NOT_MATCH, createReqDTO.getName(), definition.getName());
        }

        // 插入拓展表
        BpmProcessDefinitionExtDO definitionDO = BpmDefinitionConvert.INSTANCE.convert2(createReqDTO)
                .setProcessDefinitionId(definition.getId());
        processDefinitionMapper.insert(definitionDO);
        return definition.getId();
    }

    @Override
    public void updateProcessDefinitionState(String id, Integer state) {
        // 激活
        if (Objects.equals(SuspensionState.ACTIVE.getStateCode(), state)) {
            repositoryService.activateProcessDefinitionById(id, true, null);
            return;
        }
        // 挂起
        if (Objects.equals(SuspensionState.SUSPENDED.getStateCode(), state)) {
            repositoryService.suspendProcessDefinitionById(id, true, null);
            return;
        }
        log.error("[updateProcessDefinitionState][流程定义({}) 修改未知状态({})]", id, state);
    }

}
