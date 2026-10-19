package cn.iocoder.yudao.module.oa.controller.admin.note.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - OA 笔记目录新增/修改 Request VO")
@Data
public class OaNoteCategorySaveReqVO {

    @Schema(description = "目录编号", example = "1024")
    private Long id;

    @Schema(description = "目录名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "工作")
    @NotBlank(message = "目录名称不能为空")
    @Size(max = 255, message = "目录名称长度不能超过 255 个字符")
    private String name;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "显示顺序不能为空")
    private Integer sort;

}
