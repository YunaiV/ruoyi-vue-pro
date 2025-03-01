package cn.iocoder.yudao.module.highway.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 项目管理新增/修改 Request VO")
@Data
public class ProjectSaveReqVO {

    @Schema(description = "编号", example = "10969")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "项目编号不能为空")
    private String code;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "项目名称不能为空")
    private String pname;

    @Schema(description = "项目描述")
    private String description;

}