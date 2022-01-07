package cn.iocoder.yudao.adminserver.modules.bpm.service.task;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.instance.BpmProcessInstanceCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.task.listener.BpmProcessInstanceEventListener;

import javax.validation.Valid;

/**
 * 流程实例 Service 接口
 *
 * @author 芋道源码
 */
public interface BpmProcessInstanceService {

    /**
     * 创建流程实例
     *
     * @param userId 用户编号
     * @param createReqVO 创建信息
     * @return 实例的编号
     */
    String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqVO createReqVO);

    void getMyProcessInstancePage(Long userId);

}
