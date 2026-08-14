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
     * 发起人取消运行中的流程实例（提供给内部）
     *
     * @param userId 发起人用户编号
     * @param processInstanceId 流程实例编号
     * @param reason 取消原因
     */
    void cancelProcessInstanceByStartUser(Long userId, String processInstanceId, String reason);

}
