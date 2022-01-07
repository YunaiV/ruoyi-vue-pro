package cn.iocoder.yudao.adminserver.modules.bpm.service.task;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.instance.BpmProcessInstanceCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.instance.BpmProcessInstanceMyPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.instance.BpmProcessInstancePageItemRespVO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

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

    /**
     * 获得流程实例的分页
     *
     * @param userId 用户编号
     * @param pageReqVO 分页请求
     * @return 流程实例的分页
     */
    PageResult<BpmProcessInstancePageItemRespVO> getMyProcessInstancePage(Long userId,
                                                                          @Valid BpmProcessInstanceMyPageReqVO pageReqVO);

}
