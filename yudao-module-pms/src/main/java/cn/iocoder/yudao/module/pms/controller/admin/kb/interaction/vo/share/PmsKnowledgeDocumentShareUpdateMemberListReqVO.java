package cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.share;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 知识库文档分享成员列表更新 Request VO")
@Data
public class PmsKnowledgeDocumentShareUpdateMemberListReqVO {

    @Schema(description = "文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "文档编号不能为空")
    private Long documentId;

    @Schema(description = "内部分享成员用户编号列表")
    private List<@NotNull(message = "分享成员用户编号不能为空") Long> shareUserIds;

}
