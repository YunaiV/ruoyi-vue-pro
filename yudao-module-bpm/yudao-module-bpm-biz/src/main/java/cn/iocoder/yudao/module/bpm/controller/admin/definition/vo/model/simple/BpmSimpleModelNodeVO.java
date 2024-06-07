package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 仿钉钉流程设计模型节点 VO")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BpmSimpleModelNodeVO {

    @Schema(description = "模型节点编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "StartEvent_1")
    @NotEmpty(message = "模型节点编号不能为空")
    private String id;

    @Schema(description = "模型节点类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "模型节点类型不能为空")
    @InEnum(BpmSimpleModelNodeType.class)
    private Integer type;

    @Schema(description = "模型节点名称", example = "领导审批")
    private String name;

    // TODO @jason：要不改成 placeholder 和一般 Element-Plus 组件一致。占位符，用于展示。@芋艿。这个不是 placeholder 占位符的含义。节点配置后。节点展示的内容，不知道取什么名字好???
    // TODO @jason：【回复】占位文本（showText）是指当一个文本框没有被 focus 的时候显示的是提示文字，当他被点击之后就显示空白。。。虽然不是完全精准，但是 placeholder 相对正式点~
    @Schema(description = "节点展示内容", example = "指定成员: 芋道源码")
    private String showText;

    @Schema(description = "子节点")
    private BpmSimpleModelNodeVO childNode; // 补充说明：在该模型下，子节点有且仅有一个，不会有多个

    @Schema(description = "条件节点")
    private List<BpmSimpleModelNodeVO> conditionNodes; // 补充说明：有且仅有条件、并行、包容等分支会使用

    @Schema(description = "节点的属性")
    private Map<String, Object> attributes; // TODO @jason：建议是字段分拆下；类似说：

    /**
     * 附加节点 Id, 该节点不从前端传入。 由程序生成. 由于当个节点无法完成功能。 需要附加节点来完成。
     * 例如： 会签时需要按拒绝人数来终止流程。 需要 userTask + ServiceTask 两个节点配合完成。 serviceTask 由后端生成。
     */
    private String attachNodeId;
    // Map<String, Integer> formPermissions; 表单权限；仅发起、审批、抄送节点会使用
    // Integer approveMethod; 审批方式；仅审批节点会使用
    // TODO @jason 后面和前端一起调整一下
    // TODO @芋艿：审批人的选择；
    // TODO @芋艿：没有人的策略？
    // TODO @芋艿：审批拒绝的策略？
    // TODO @芋艿：配置的可操作列表？
    // TODO @芋艿：超时配置；要支持指定时间点、指定时间间隔；
    // TODO @芋艿：条件；建议可以固化的一些选项；然后有个表达式兜底；要支持

}
