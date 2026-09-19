package cn.iocoder.yudao.module.oa.controller.admin.file.vo.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;

@Schema(description = "管理后台 - 云盘重命名 Request VO")
@Data
public class OaFileNodeRenameReqVO {

    @Schema(description = "节点编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "节点编号不能为空")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "办公用品盘点结果.pdf")
    @NotBlank(message = "名称不能为空")
    @Size(max = 255, message = "名称长度不能超过 255 个字符")
    private String name;

}
