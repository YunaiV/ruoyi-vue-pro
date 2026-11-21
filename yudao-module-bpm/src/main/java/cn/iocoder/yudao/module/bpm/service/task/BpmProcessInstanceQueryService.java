package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.*;
import jakarta.validation.Valid;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 流程实例【读】Service 接口
 *
 * @author 芋道源码
 */
public interface BpmProcessInstanceQueryService {

    /**
     * 获得流程实例
     *
     * @param id 流程实例的编号
     * @return 流程实例
     */
    ProcessInstance getProcessInstance(String id);

    /**
     * 获得流程实例列表
     *
     * @param ids 流程实例的编号集合
     * @return 流程实例列表
     */
    List<ProcessInstance> getProcessInstances(Set<String> ids);

    /**
     * 获得流程实例 Map
     *
     * @param ids 流程实例的编号集合
     * @return 流程实例列表 Map
     */
    default Map<String, ProcessInstance> getProcessInstanceMap(Set<String> ids) {
        return convertMap(getProcessInstances(ids), ProcessInstance::getProcessInstanceId);
    }

    /**
     * 获得历史的流程实例
     *
     * @param id 流程实例的编号
     * @return 历史的流程实例
     */
    HistoricProcessInstance getHistoricProcessInstance(String id);

    /**
     * 获得历史的流程实例列表
     *
     * @param ids 流程实例的编号集合
     * @return 历史的流程实例列表
     */
    List<HistoricProcessInstance> getHistoricProcessInstances(Set<String> ids);

    /**
     * 获得历史的流程实例 Map
     *
     * @param ids 流程实例的编号集合
     * @return 历史的流程实例列表 Map
     */
    default Map<String, HistoricProcessInstance> getHistoricProcessInstanceMap(Set<String> ids) {
        return convertMap(getHistoricProcessInstances(ids), HistoricProcessInstance::getId);
    }

    /**
     * 获得流程实例的分页
     *
     * @param userId    用户编号
     * @param pageReqVO 分页请求
     * @return 流程实例的分页
     */
    PageResult<HistoricProcessInstance> getProcessInstancePage(Long userId,
                                                               @Valid BpmProcessInstancePageReqVO pageReqVO);

    /**
     * 获取审批详情。
     * <p>
     * 可以是准备发起的流程、进行中的流程、已经结束的流程
     *
     * @param loginUserId  登录人的用户编号
     * @param reqVO 请求信息
     * @return 流程实例的进度
     */
    BpmApprovalDetailRespVO getApprovalDetail(Long loginUserId, @Valid BpmApprovalDetailReqVO reqVO);

    /**
     * 获取下一个执行节点信息
     *
     * @param loginUserId 登录人的用户编号
     * @param reqVO 请求信息
     * @return 下一个执行节点信息
     */
    List<BpmApprovalDetailRespVO.ActivityNode> getNextApprovalNodes(Long loginUserId, @Valid BpmApprovalDetailReqVO reqVO);

    /**
     * 获取流程实例的 BPMN 模型视图
     *
     * @param id 流程实例的编号
     * @return BPMN 模型视图
     */
    BpmProcessInstanceBpmnModelViewRespVO getProcessInstanceBpmnModelView(String id);

}
