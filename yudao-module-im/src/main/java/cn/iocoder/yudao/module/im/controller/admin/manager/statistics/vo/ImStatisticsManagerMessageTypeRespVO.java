package cn.iocoder.yudao.module.im.controller.admin.manager.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IM 数据看板内容类型分布项 Response VO")
@Data
public class ImStatisticsManagerMessageTypeRespVO {

    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer type; // 参见 ImContentTypeEnum 枚举类

    @Schema(description = "消息数", requiredMode = Schema.RequiredMode.REQUIRED, example = "8000")
    private Long value;

}
