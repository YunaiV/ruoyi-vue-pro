package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.*;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceDeleteReasonEnum;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 流程实例 Service 接口
 *
 * @author 芋道源码
 */
public interface BpmProcessInstanceService {

    /**
     * 创建流程实例（提供给前端）
     *
     * @param userId 用户编号
     * @param createReqVO 创建信息
     * @return 实例的编号
     */
    String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqVO createReqVO);

    /**
     * 创建流程实例（提供给内部）
     *
     * @param userId 用户编号
     * @param createReqDTO 创建信息
     * @return 实例的编号
     */
    String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqDTO createReqDTO);

    /**
     * 取消流程实例
     *
     * @param userId 用户编号
     * @param cancelReqVO 取消信息
     */
    void cancelProcessInstance(Long userId, @Valid BpmProcessInstanceCancelReqVO cancelReqVO);

    /**
     * 删除流程实例
     *
     * @param id 流程编号
     * @param reason 删除原因。可选 {@link BpmProcessInstanceDeleteReasonEnum}
     */
    @Deprecated
    void deleteProcessInstance(String id, String reason);

    /**
     * 获得流程实例的分页
     *
     * @param userId 用户编号
     * @param pageReqVO 分页请求
     * @return 流程实例的分页
     */
    PageResult<BpmProcessInstancePageItemRespVO> getMyProcessInstancePage(Long userId,
                                                                          @Valid BpmProcessInstanceMyPageReqVO pageReqVO);

    /**
     * 获得流程实例 VO 信息
     *
     * @param id 流程实例的编号
     * @return 流程实例
     */
    BpmProcessInstanceRespVO getProcessInstanceVO(String id);

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
        return CollectionUtils.convertMap(getProcessInstances(ids), ProcessInstance::getProcessInstanceId);
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
        return CollectionUtils.convertMap(getHistoricProcessInstances(ids), HistoricProcessInstance::getId);
    }

    /**
     * 创建 ProcessInstance 拓展记录
     *
     * @param instance 流程任务
     */
    void createProcessInstanceExt(org.activiti.api.process.model.ProcessInstance instance);

    /**
     * 更新 ProcessInstance 拓展记录
     *
     * @param instance 流程任务
     */
    void updateProcessInstanceExt(org.activiti.api.process.model.ProcessInstance instance);

    /**
     * 更新 ProcessInstance 拓展记录为取消
     *
     * @param instance 流程任务
     * @param reason 取消原因
     */
    void updateProcessInstanceExtCancel(org.activiti.api.process.model.ProcessInstance instance, String reason);

    /**
     * 更新 ProcessInstance 拓展记录为完成
     *
     * @param instance 流程任务
     */
    void updateProcessInstanceExtComplete(org.activiti.api.process.model.ProcessInstance instance);

    /**
     * 更新 ProcessInstance 拓展记录为不通过
     *
     * @param id 流程编号
     * @param comment 理由。例如说，审批不通过时，需要传递该值
     */
    void updateProcessInstanceExtReject(String id, String comment);

}
