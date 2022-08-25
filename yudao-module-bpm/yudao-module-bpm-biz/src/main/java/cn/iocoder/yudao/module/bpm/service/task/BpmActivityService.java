package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.activity.BpmActivityRespVO;
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
    List<BpmActivityRespVO> getActivityListByProcessInstanceId(String processInstanceId);

    /**
     * 获得执行编号对应的活动实例
     *
     * @param executionId 执行编号
     * @return 活动实例
     */
    List<HistoricActivityInstance> getHistoricActivityListByExecutionId(String executionId);

}
