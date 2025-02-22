package cn.iocoder.yudao.module.bpm.api.task;

import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;

import jakarta.validation.Valid;

/**
 * 流程实例 Api 接口
 *
 * @author 芋道源码
 */
public interface BpmProcessInstanceApi {

    /**
     * 创建流程实例（提供给内部）
     *
     * @param userId 用户编号
     * @param reqDTO 创建信息
     * @return 实例的编号
     */
    String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqDTO reqDTO);


    /**
     * 异步 HTTP 请求触发器回调, 为了唤醒流程继续执行
     *
     * @param processInstanceId 流程实例编号
     * @param callbackId 回调编号, 对应 ReceiveTask Id
     */
    void asyncHttpTriggerCallback(String processInstanceId, String callbackId);
}
