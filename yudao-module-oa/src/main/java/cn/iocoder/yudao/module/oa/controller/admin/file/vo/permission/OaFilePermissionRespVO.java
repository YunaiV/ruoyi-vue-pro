package cn.iocoder.yudao.module.oa.controller.admin.file.vo.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 云盘共享权限 Response VO")
@Data
public class OaFilePermissionRespVO {

    @Schema(description = "权限编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "节点编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long nodeId;

    @Schema(description = "主体类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer subjectType;

    @Schema(description = "主体编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long subjectId;

    @Schema(description = "权限级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer level;

    @Schema(description = "是否继承", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean inherit;

    @Schema(description = "到期时间", type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime expireTime;

}
