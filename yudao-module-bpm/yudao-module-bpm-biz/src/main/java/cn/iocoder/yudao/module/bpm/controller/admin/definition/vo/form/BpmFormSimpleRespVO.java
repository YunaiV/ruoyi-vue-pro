package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(title = "管理后台 - 流程表单精简 Response VO")
@Data
public class BpmFormSimpleRespVO {

    @Schema(title = "表单编号", required = true, example = "1024")
    private Long id;

    @Schema(title = "表单名称", required = true, example = "芋道")
    private String name;

}
