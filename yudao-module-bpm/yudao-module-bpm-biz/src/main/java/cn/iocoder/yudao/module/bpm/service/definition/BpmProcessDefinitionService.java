package cn.iocoder.yudao.module.bpm.service.definition;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionPageReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * Flowable流程定义接口
 *
 * @author yunlong.li
 * @author ZJQ
 * @author 芋道源码
 */
public interface BpmProcessDefinitionService {

    /**
     * 获得流程定义分页
     *
     * @param pageReqVO 分页入参
     * @return 流程定义 Page
     */
    PageResult<ProcessDefinition> getProcessDefinitionPage(BpmProcessDefinitionPageReqVO pageReqVO);

    /**
     * 获得流程定义列表
     *
     * @param suspensionState 中断状态
     * @return 流程定义列表
     */
    List<ProcessDefinition> getProcessDefinitionListBySuspensionState(Integer suspensionState);

    /**
     * 基于流程模型，创建流程定义
     *
     * @param model 流程模型
     * @param modelMetaInfo 流程模型元信息
     * @param bpmnBytes BPMN XML 字节数组
     * @param simpleBytes SIMPLE Model JSON 字节数组
     * @param form 表单
     * @return 流程编号
     */
    String createProcessDefinition(Model model, BpmModelMetaInfoVO modelMetaInfo,
                                   byte[] bpmnBytes, byte[] simpleBytes, BpmFormDO form);

    /**
     * 更新流程定义状态
     *
     * @param id 流程定义的编号
     * @param state 状态
     */
    void updateProcessDefinitionState(String id, Integer state);

    /**
     * 获得流程定义对应的 BPMN
     *
     * @param id 流程定义编号
     * @return BPMN
     */
    BpmnModel getProcessDefinitionBpmnModel(String id);

    /**
     * 获得流程定义的信息
     *
     * @param id 流程定义编号
     * @return 流程定义信息
     */
    BpmProcessDefinitionInfoDO getProcessDefinitionInfo(String id);

    /**
     * 获得流程定义的信息 List
     *
     * @param ids 流程定义编号数组
     * @return 流程额定义信息数组
     */
    List<BpmProcessDefinitionInfoDO> getProcessDefinitionInfoList(Collection<String> ids);

    default Map<String, BpmProcessDefinitionInfoDO> getProcessDefinitionInfoMap(Set<String> ids) {
        return convertMap(getProcessDefinitionInfoList(ids), BpmProcessDefinitionInfoDO::getProcessDefinitionId);
    }

    /**
     * 获得流程定义编号对应的 ProcessDefinition
     *
     * @param id 流程定义编号
     * @return 流程定义
     */
    ProcessDefinition getProcessDefinition(String id);

    /**
     * 获得 ids 对应的 ProcessDefinition 数组
     *
     * @param ids 编号的数组
     * @return 流程定义的数组
     */
    List<ProcessDefinition> getProcessDefinitionList(Set<String> ids);

    default Map<String, ProcessDefinition> getProcessDefinitionMap(Set<String> ids) {
        return convertMap(getProcessDefinitionList(ids), ProcessDefinition::getId);
    }

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
     * 判断用户是否可以使用该流程定义，进行流程的发起
     *
     * @param processDefinition 流程定义
     * @param userId 用户编号
     * @return 是否可以发起流程
     */
    boolean canUserStartProcessDefinition(BpmProcessDefinitionInfoDO processDefinition, Long userId);

    /**
     * 获得 ids 对应的 Deployment Map
     *
     * @param ids 部署编号的数组
     * @return 流程部署 Map
     */
    default Map<String, Deployment> getDeploymentMap(Set<String> ids) {
        return convertMap(getDeploymentList(ids), Deployment::getId);
    }

    /**
     * 获得 ids 对应的 Deployment 数组
     *
     * @param ids 部署编号的数组
     * @return 流程部署的数组
     */
    List<Deployment> getDeploymentList(Set<String> ids);

    /**
     * 获得 id 对应的 Deployment
     *
     * @param id 部署编号
     * @return 流程部署
     */
    Deployment getDeployment(String id);

}
