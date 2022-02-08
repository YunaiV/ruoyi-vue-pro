package cn.iocoder.yudao.module.bpm.service.definition;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
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
public interface FlowableProcessDefinitionService {

    /**
     * 获得 deploymentIds 对应的 ProcessDefinition 数组
     *
     * @param deploymentIds 部署编号的数组
     * @return 流程定义的数组
     */
    List<ProcessDefinition> getProcessDefinitionListByDeploymentIds(Set<String> deploymentIds);

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
}
