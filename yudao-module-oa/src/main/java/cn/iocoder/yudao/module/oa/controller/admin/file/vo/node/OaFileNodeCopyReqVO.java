package cn.iocoder.yudao.module.oa.controller.admin.file.vo.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 云盘复制 Request VO")
@Data
public class OaFileNodeCopyReqVO {

    @Schema(description = "节点编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "节点编号不能为空")
    private Long id;

    @Schema(description = "目标目录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "目标目录编号不能为空")
    private Long parentId;

}
