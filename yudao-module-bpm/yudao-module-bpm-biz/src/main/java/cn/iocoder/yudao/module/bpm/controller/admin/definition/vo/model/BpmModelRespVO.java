package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 流程模型的创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmModelRespVO extends BpmModelBaseVO {

    @Schema(description = "编号", required = true, example = "1024")
    private String id;

    @Schema(description = "BPMN XML", required = true)
    private String bpmnXml;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
