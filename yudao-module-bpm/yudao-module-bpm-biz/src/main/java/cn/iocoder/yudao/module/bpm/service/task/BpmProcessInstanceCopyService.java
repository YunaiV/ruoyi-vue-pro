package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyPageReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmProcessInstanceCopyDO;

import java.util.Collection;
import java.util.Set;

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
     * 流程实例的抄送
     *
     * @param userIds 抄送的用户编号
     * @param processInstanceId 流程编号
     * @param activityId 流程活动编号 id (对应 BPMN XML 节点 Id)
     * // TODO 芋艿这个 taskId 是不是可以不要了
     * @param taskId 任务编号
     * @param taskName 任务名称
     */
    void createProcessInstanceCopy(Collection<Long> userIds, String processInstanceId, String activityId, String taskId, String taskName);

    /**
     * 获得抄送的流程的分页
     *
     * @param userId 当前登录用户
     * @param pageReqVO 分页请求
     * @return 抄送的分页结果
     */
    PageResult<BpmProcessInstanceCopyDO> getProcessInstanceCopyPage(Long userId,
                                                                    BpmProcessInstanceCopyPageReqVO pageReqVO);
    /**
     * 通过流程实例和流程活动编号获取抄送人的 Id
     *
     * @param processInstanceId 流程实例 Id
     * @param activityId 流程活动编号 Id
     * @return 抄送人 Ids
     */
    Set<Long> getCopyUserIds(String processInstanceId, String activityId);

}
