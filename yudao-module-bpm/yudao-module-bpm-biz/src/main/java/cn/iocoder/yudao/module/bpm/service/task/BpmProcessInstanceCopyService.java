package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyPageReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmProcessInstanceCopyDO;

import java.util.Collection;

/**
 * 流程抄送 Service 接口
 *
 * 现在是在审批的时候进行流程抄送
 */
public interface BpmProcessInstanceCopyService {

    /**
     * 流程实例的抄送
     *
     * @param userIds 抄送的用户编号
     * @param taskId 流程任务编号
     */
    void createProcessInstanceCopy(Collection<Long> userIds, String taskId);

    /**
     * 获得抄送的流程的分页
     *
     * @param userId 当前登录用户
     * @param pageReqVO 分页请求
     * @return 抄送的分页结果
     */
    PageResult<BpmProcessInstanceCopyDO> getProcessInstanceCopyPage(Long userId,
                                                                    BpmProcessInstanceCopyPageReqVO pageReqVO);

}
