package cn.iocoder.yudao.module.bpm.service.task.bo;

import lombok.Data;

import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceProgressRespVO.ApproveNodeInfo;

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
    private List<ApproveNodeInfo> approveNodes;

    /**
     * 进行中的节点 ID 数组
     */
    private Set<String> runNodeIds;

}
