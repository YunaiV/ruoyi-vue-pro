package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceProgressRespVO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import org.flowable.engine.history.HistoricActivityInstance;

import java.util.List;

/**
 * BPM 活动实例 Service 接口
 *
 * @author 芋道源码
 */
public interface BpmActivityService {

    /**
     * 获得指定流程实例的活动实例列表
     *
     * @param processInstanceId 流程实例的编号
     * @return 活动实例列表
     */
    List<HistoricActivityInstance> getActivityListByProcessInstanceId(String processInstanceId);

    /**
     * 获得执行编号对应的活动实例
     *
     * @param executionId 执行编号
     * @return 活动实例
     */
    List<HistoricActivityInstance> getHistoricActivityListByExecutionId(String executionId);

    /**
     * 获取活动的用户列表。
     *
     * 例如：抄送人列表、审批人列表
     *
     * @param historicActivity     活动
     * @param isMultiInstance      是否多实例 (会签，或签 )
     * @param historicActivityList 某个流程实例的所有活动列表
     * @return 用户列表
     */
    List<BpmProcessInstanceProgressRespVO.User> getHistoricActivityUserList(HistoricActivityInstance historicActivity,
                                                                            Boolean isMultiInstance,
                                                                            List<HistoricActivityInstance> historicActivityList);

    /**
     * 获取活动的进度状态
     *
     * @param historicActivity     活动
     * @param isMultiInstance      是否多实例 (会签，或签 )
     * @param historicActivityList 某个流程实例的所有活动列表
     * @return 活动的进度状态
     */
    Integer getHistoricActivityProgressStatus(HistoricActivityInstance historicActivity,
                                              Boolean isMultiInstance,
                                              List<HistoricActivityInstance> historicActivityList);

    /**
     * 获取未执行活动的进度状态
     *
     * @param processInstanceStatus 流程实例的状态 {@link BpmProcessInstanceStatusEnum}
     * @return 活动的进度状态
     */
    Integer getNotRunActivityProgressStatus(Integer processInstanceStatus);

    /**
     * 获取未执行活动的用户列表
     *
     * @param processInstanceId 流程实例的编号
     * @param processInstanceStatus 流程实例的状态 {@link BpmProcessInstanceStatusEnum}
     * @param candidateStrategy 活动的候选人策略
     * @param candidateParam  活动的候选人参数
     * @return 用户列表
     */
    List<BpmProcessInstanceProgressRespVO.User> getNotRunActivityUserList(String processInstanceId,
                                                                          Integer processInstanceStatus,
                                                                          Integer candidateStrategy,
                                                                          String candidateParam);

}
