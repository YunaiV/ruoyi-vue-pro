package cn.iocoder.yudao.adminserver.modules.bpm.service.definition;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.BpmProcessDefinitionPageItemRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.BpmProcessDefinitionPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.dto.BpmDefinitionCreateReqDTO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 流程定义接口
 *
 * @author yunlong.li
 * @author ZJQ
 */
public interface BpmDefinitionService {

    /**
     * 获得流程定义分页
     *
     * @param pageReqVO 分页入参
     * @return 流程定义 Page
     */
    PageResult<BpmProcessDefinitionPageItemRespVO> getDefinitionPage(BpmProcessDefinitionPageReqVO pageReqVO);

    /**
     * 获得流程定义对应的 BPMN XML
     *
     * @param id 流程定义编号
     * @return BPMN XML
     */
    String getDefinitionBpmnXML(String id);

    /**
     * 获得 Bpmn 模型
     *
     * @param processDefinitionId 流程定义的编号
     * @return Bpmn 模型
     */
    BpmnModel getBpmnModel(String processDefinitionId);

    /**
     * 获得编号对应的 ProcessDefinition
     *
     * @param id 编号
     * @return 流程定义
     */
    ProcessDefinition getDefinition(String id);

    /**
     * 获得 id 对应的 Deployment
     *
     * @param id 部署编号
     * @return 流程部署
     */
    Deployment getDeployment(String id);

    /**
     * 获得 ids 对应的 Deployment 数组
     *
     * @param ids 部署编号的数组
     * @return 流程部署的数组
     */
    List<Deployment> getDeployments(Set<String> ids);

    /**
     * 获得 ids 对应的 Deployment Map
     *
     * @param ids 部署编号的数组
     * @return 流程部署 Map
     */
    default Map<String, Deployment> getDeploymentMap(Set<String> ids) {
        return CollectionUtils.convertMap(getDeployments(ids), Deployment::getId);
    }

    /**
     * 获得 deploymentIds 对应的 ProcessDefinition 数组
     *
     * @param deploymentIds 部署编号的数组
     * @return 流程定义的数组
     */
    List<ProcessDefinition> getDefinitionListByDeploymentIds(Set<String> deploymentIds);

    /**
     * 创建流程定义
     *
     * @param createReqDTO 创建信息
     * @return 流程编号
     */
    String createDefinition(@Valid BpmDefinitionCreateReqDTO createReqDTO);

}
