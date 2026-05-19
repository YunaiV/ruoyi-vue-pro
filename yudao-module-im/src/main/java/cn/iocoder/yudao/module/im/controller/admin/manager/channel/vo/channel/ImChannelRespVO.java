package cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IM 频道 Response VO")
@Data
public class ImChannelRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "频道业务码", requiredMode = Schema.RequiredMode.REQUIRED, example = "system_notice")
    private String code;

    @Schema(description = "频道名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "系统公告")
    private String name;

    @Schema(description = "频道头像")
    private String avatar;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status; // 参见 CommonStatusEnum 枚举类

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
