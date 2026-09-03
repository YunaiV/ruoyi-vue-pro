package cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.viewrecord;

import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.PmsKnowledgeInteractionItemRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - PMS 知识最近浏览 Response VO")
@Data
public class PmsKnowledgeRecentListRespVO {

    @Schema(description = "今天浏览列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PmsKnowledgeInteractionItemRespVO> todayItems;

    @Schema(description = "昨天浏览列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PmsKnowledgeInteractionItemRespVO> yesterdayItems;

    @Schema(description = "更早 30 天浏览列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PmsKnowledgeInteractionItemRespVO> recent30DayItems;

}
