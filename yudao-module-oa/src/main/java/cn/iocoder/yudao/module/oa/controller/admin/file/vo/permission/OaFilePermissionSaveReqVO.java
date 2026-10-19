package cn.iocoder.yudao.module.oa.controller.admin.file.vo.permission;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.file.OaFilePermissionLevelEnum;
import cn.iocoder.yudao.module.oa.enums.file.OaFileSubjectTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 云盘共享权限 Request VO")
@Data
public class OaFilePermissionSaveReqVO {

    @Schema(description = "节点编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "节点编号不能为空")
    private Long nodeId;

    @Schema(description = "共享主体", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "共享主体类型不能为空")
    @InEnum(value = OaFileSubjectTypeEnum.class, message = "共享主体类型必须是 {value}")
    private Integer subjectType;

    @Schema(description = "主体编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "主体编号不能为空")
    private Long subjectId;

    @Schema(description = "权限", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "权限级别不能为空")
    @InEnum(value = OaFilePermissionLevelEnum.class, message = "权限级别必须是 {value}")
    private Integer level;

    @Schema(description = "是否继承到子目录及文件", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否继承不能为空")
    private Boolean inherit;

    @Schema(description = "到期时间", type = "integer", format = "int64", example = "1789347600000")
    @Future(message = "到期时间必须晚于当前时间")
    private LocalDateTime expireTime;

}
