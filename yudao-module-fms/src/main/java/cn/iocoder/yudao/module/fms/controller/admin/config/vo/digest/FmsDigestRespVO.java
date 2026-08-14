package cn.iocoder.yudao.module.fms.controller.admin.config.vo.digest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * FMS 常用摘要 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 常用摘要 Response VO")
@Data
public class FmsDigestRespVO {

    @Schema(description = "常用摘要编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long accountSetId;

    @Schema(description = "摘要内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "购买办公用品")
    private String content;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
