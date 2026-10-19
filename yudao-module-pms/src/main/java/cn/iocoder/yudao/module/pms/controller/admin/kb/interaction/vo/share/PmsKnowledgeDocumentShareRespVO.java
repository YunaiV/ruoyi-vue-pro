package cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.share;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - PMS 知识库文档分享 Response VO")
@Data
public class PmsKnowledgeDocumentShareRespVO {

    @Schema(description = "分享编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long documentId;

    @Schema(description = "内部分享成员用户编号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> shareUserIds;

    @Schema(description = "外部查看令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @Schema(description = "分享状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "关闭人用户编号")
    private Long closeUserId;

    @Schema(description = "关闭时间")
    private LocalDateTime closeTime;

}
