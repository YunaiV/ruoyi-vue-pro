package cn.iocoder.yudao.module.bpm.service.task.bo;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO.ApprovalNodeInfo;

/**
 * 已经进行中的审批节点 Response BO
 *
 * @author jason
 */
@Data
public class AlreadyRunApproveNodeRespBO {

    /**
     * 审批节点信息数组
     */
    private List<ApprovalNodeInfo> approveNodes;

    /**
     * 已运行的节点 ID 数组 (对应 Bpmn XML 节点 id)
     */
    private Set<String> runNodeIds;

    /**
     * 正在运行的节点的审批信息（key: activityId, value: 审批信息）
     * <p>
     * 用于依次审批，需要加上候选人信息
     */
    private Map<String, ApprovalNodeInfo> runningApprovalNodes;

}
