package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 项目合同 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ProjectConstractItemVO {

    @Schema(description = "合同名字", example = "合并名字")
    private String name;

    @Schema(description = "项目id", example = "1")
    private Long projectId;

    @Schema(description = "合同文件 URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "")
    @NotNull(message = "合同文件 URL不能为空")
    private String fileUrl;

    @Schema(description = "合同文件名", example = "芋艿")
    private String fileName;

}
