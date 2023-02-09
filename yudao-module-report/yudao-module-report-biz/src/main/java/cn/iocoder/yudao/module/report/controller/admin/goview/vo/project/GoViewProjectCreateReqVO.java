package cn.iocoder.yudao.module.report.controller.admin.goview.vo.project;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - GoView 项目创建 Request VO")
@Data
public class GoViewProjectCreateReqVO {

    @Schema(description = "项目名称", required = true, example = "王五")
    @NotEmpty(message = "项目名称不能为空")
    private String name;

}
