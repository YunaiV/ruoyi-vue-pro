package cn.iocoder.yudao.module.oa.controller.admin.note.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - OA 笔记 Response VO")
@Data
public class OaNoteRespVO {

    @Schema(description = "笔记编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "创建人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long creatorUserId;

    @Schema(description = "创建人用户昵称", example = "芋道源码")
    private String creatorUserName;

    @Schema(description = "目录编号", example = "1024")
    private Long categoryId;

    @Schema(description = "目录名称", example = "工作")
    private String categoryName;

    @Schema(description = "笔记类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer priority;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "项目会议纪要")
    private String title;

    @Schema(description = "内容", example = "记录本周会议结论与待办事项。")
    private String content;

    @Schema(description = "是否收藏", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean favorite;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    private List<String> fileUrls;

    @Schema(description = "接收人用户编号列表", example = "[1, 2]")
    private List<Long> receiverUserIds;

    @Schema(description = "接收人用户昵称列表", example = "[\"张三\", \"李四\"]")
    private List<String> receiverUserNames;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

}
