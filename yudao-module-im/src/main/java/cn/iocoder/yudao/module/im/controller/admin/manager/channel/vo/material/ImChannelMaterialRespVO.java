package cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.material;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IM 频道素材 Response VO")
@Data
public class ImChannelMaterialRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "频道编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long channelId;

    @Schema(description = "频道名称（关联查询填充）")
    private String channelName;

    @Schema(description = "内容类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type; // 参见 ImChannelMaterialTypeEnum 枚举类

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "封面图")
    private String coverUrl;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "正文；富文本 HTML")
    private String content;

    @Schema(description = "跳转链接")
    private String url;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
