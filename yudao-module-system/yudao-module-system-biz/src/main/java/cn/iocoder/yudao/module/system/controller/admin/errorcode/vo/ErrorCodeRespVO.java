package cn.iocoder.yudao.module.system.controller.admin.errorcode.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(title = "管理后台 - 错误码 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErrorCodeRespVO extends ErrorCodeBaseVO {

    @Schema(title = "错误码编号", required = true, example = "1024")
    private Long id;

    @Schema(title = "错误码类型", required = true, example = "1", description = "参见 ErrorCodeTypeEnum 枚举类")
    private Integer type;

    @Schema(title = "创建时间", required = true)
    private LocalDateTime createTime;

}
