package cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - PMS 知识库分组新增/修改 Request VO")
@Data
public class PmsKnowledgeGroupSaveReqVO {

    @Schema(description = "知识库分组编号", example = "1024")
    private Long id;

    @Schema(description = "分组名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品知识库")
    @NotBlank(message = "分组名称不能为空")
    @Size(max = 100, message = "分组名称不能超过 100 个字符")
    private String name;

}
