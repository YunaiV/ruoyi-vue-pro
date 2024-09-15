package cn.iocoder.yudao.module.bpm.service.task.bo;

import lombok.Data;

import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceProgressRespVO.ApproveNodeInfo;

/**
 * @author jason
 */
@Data
public class AlreadyRunApproveNodeRespBO {

    private List<ApproveNodeInfo> approveNodeList;

    private Set<String> runNodeIds;

}
