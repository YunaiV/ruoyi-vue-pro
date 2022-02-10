package cn.iocoder.yudao.module.bpm.service.definition;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;

import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * Flowable流程定义接口
 *
 * @author yunlong.li
 * @author ZJQ
 * @author 芋道源码
 */
public interface BpmProcessDefinitionService extends BpmProcessDefinitionCommonService {

    /**
     * 获得编号对应的 ProcessDefinition
     *
     * @param id 编号
     * @return 流程定义
     */
    ProcessDefinition getProcessDefinition(String id);

    /**
     * 获得 deploymentId 对应的 ProcessDefinition
     *
     * @param deploymentId 部署编号
     * @return 流程定义
     */
    ProcessDefinition getProcessDefinitionByDeploymentId(String deploymentId);

    /**
     * 获得 deploymentIds 对应的 ProcessDefinition 数组
     *
     * @param deploymentIds 部署编号的数组
     * @return 流程定义的数组
     */
    List<ProcessDefinition> getProcessDefinitionListByDeploymentIds(Set<String> deploymentIds);

    /**
     * 获得流程定义标识对应的激活的流程定义
     *
     * @param key 流程定义的标识
     * @return 流程定义
     */
    ProcessDefinition getActiveProcessDefinition(String key);

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
     * 获得 ids 对应的 Deployment 数组
     *
     * @param ids 部署编号的数组
     * @return 流程部署的数组
     */
    List<Deployment> getDeployments(Set<String> ids);

    /**
     * 获得 id 对应的 Deployment
     *
     * @param id 部署编号
     * @return 流程部署
     */
    Deployment getDeployment(String id);

    /**
     * 获得 Bpmn 模型
     *
     * @param processDefinitionId 流程定义的编号
     * @return Bpmn 模型
     */
    BpmnModel getBpmnModel(String processDefinitionId);
}
