package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.*;
import jakarta.validation.Valid;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.Collection;
import java.util.Map;

/**
 * 流程实例【写】Service 接口
 *
 * @author 芋道源码
 */
public interface BpmProcessInstanceUpdateService {

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
     * @param userId      用户编号
     * @param cancelReqVO 取消信息
     */
    void cancelProcessInstanceByAdmin(Long userId, BpmProcessInstanceCancelReqVO cancelReqVO);

    /**
     * 更新 ProcessInstance 为不通过
     *
     * @param processInstance 流程实例
     * @param reason          理由。例如说，审批不通过时，需要传递该值
     */
    void updateProcessInstanceReject(ProcessInstance processInstance, String reason);

    /**
     * 更新 ProcessInstance 的变量
     *
     * @param id 流程编号
     * @param variables 流程变量
     */
    void updateProcessInstanceVariables(String id, Map<String, Object> variables);

    /**
     * 删除 ProcessInstance 的变量
     *
     * @param id  流程编号
     * @param variableNames 流程变量名
     */
    void removeProcessInstanceVariables(String id, Collection<String> variableNames);

    // ========== Event 事件相关方法 ==========

    /**
     * 处理 ProcessInstance 完成事件，例如说：审批通过、不通过、取消
     *
     * @param instance 流程任务
     */
    void processProcessInstanceCompleted(ProcessInstance instance);

    /**
     * 处理 ProcessInstance 开始事件，例如说：流程前置通知
     *
     * @param instance 流程任务
     */
    void processProcessInstanceCreated(ProcessInstance instance);
}
