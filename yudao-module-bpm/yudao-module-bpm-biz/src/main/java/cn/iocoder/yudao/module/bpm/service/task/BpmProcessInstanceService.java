package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCancelReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstancePageReqVO;
import jakarta.validation.Valid;
import org.flowable.engine.delegate.event.FlowableCancelledEvent;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 流程实例 Service 接口
 *
 * @author 芋道源码
 */
public interface BpmProcessInstanceService {

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
     * 创建流程实例（提供给前端）
     *
     * @param userId      用户编号
     * @param createReqVO 创建信息
     * @return 实例的编号
     */
    String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqVO createReqVO);

    /**
     * 创建流程实例（提供给内部）
     *
     * @param userId       用户编号
     * @param createReqDTO 创建信息
     * @return 实例的编号
     */
    String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqDTO createReqDTO);

    /**
     * 发起人取消流程实例
     *
     * @param userId      用户编号
     * @param cancelReqVO 取消信息
     */
    void cancelProcessInstanceByStartUser(Long userId, @Valid BpmProcessInstanceCancelReqVO cancelReqVO);

    /**
     * 管理员取消流程实例
     *
     * @param userId           用户编号
     * @param cancelReqVO 取消信息
     */
    void cancelProcessInstanceByAdmin(Long userId, BpmProcessInstanceCancelReqVO cancelReqVO);

    /**
     * 更新 ProcessInstance 拓展记录为取消
     *
     * @param event 流程取消事件
     */
    void updateProcessInstanceWhenCancel(FlowableCancelledEvent event);

    /**
     * 更新 ProcessInstance 拓展记录为完成
     *
     * @param instance 流程任务
     */
    void updateProcessInstanceWhenApprove(ProcessInstance instance);

    /**
     * 更新 ProcessInstance 拓展记录为不通过
     *
     * @param id     流程编号
     * @param reason 理由。例如说，审批不通过时，需要传递该值
     */
    void updateProcessInstanceReject(String id, String reason);

}
