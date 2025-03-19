package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Schema(description = "管理后台 - 流程示例的 BPMN 视图 Response VO")
@Data
public class BpmProcessInstanceBpmnModelViewRespVO {

    // ========== 基本信息 ==========

    @Schema(description = "流程实例信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private BpmProcessInstanceRespVO processInstance;

    @Schema(description = "任务列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<BpmTaskRespVO> tasks;

    @Schema(description = "BPMN XML", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bpmnXml;

    @Schema(description = "SIMPLE 模型")
    private BpmSimpleModelNodeVO simpleModel;

    // ========== 进度信息 ==========

    @Schema(description = "进行中的活动节点编号集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<String> unfinishedTaskActivityIds; // 只包括 UserTask

    @Schema(description = "已经完成的活动节点编号集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<String> finishedTaskActivityIds; // 包括 UserTask、Gateway 等，不包括 SequenceFlow

    @Schema(description = "已经完成的连线节点编号集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<String> finishedSequenceFlowActivityIds; // 只包括 SequenceFlow

    @Schema(description = "已经拒绝的活动节点编号集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<String> rejectedTaskActivityIds; // 只包括 UserTask

}
