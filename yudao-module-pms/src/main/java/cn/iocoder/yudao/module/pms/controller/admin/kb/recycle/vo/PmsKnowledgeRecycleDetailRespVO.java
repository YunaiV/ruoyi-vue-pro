package cn.iocoder.yudao.module.pms.controller.admin.kb.recycle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - PMS 知识库回收站详情 Response VO")
@Data
public class PmsKnowledgeRecycleDetailRespVO {

    @Schema(description = "根回收站记录")
    private PmsKnowledgeRecycleRespVO root;

    @Schema(description = "级联删除内容")
    private List<Item> children;

    @Data
    public static class Item {

        @Schema(description = "内容编号", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;

        @Schema(description = "内容类型", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer type;

        @Schema(description = "内容名称", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @Schema(description = "父文档或父文件夹编号")
        private Long parentId;

        @Schema(description = "所属文件夹编号")
        private Long folderId;

        @Schema(description = "删除时间")
        private LocalDateTime deleteTime;

    }
}
