package cn.iocoder.yudao.module.infra.controller.admin.demo.demo03.erp.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 学生新增/修改 Request VO")
@Data
public class Demo03StudentErpSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8525")
    private Long id;

    @Schema(description = "名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "名字不能为空")
    private String name;

    @Schema(description = "性别", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "性别不能为空")
    private Integer sex;

    @Schema(description = "出生日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "出生日期不能为空")
    private LocalDateTime birthday;

    @Schema(description = "简介", requiredMode = Schema.RequiredMode.REQUIRED, example = "随便")
    @NotEmpty(message = "简介不能为空")
    private String description;

}