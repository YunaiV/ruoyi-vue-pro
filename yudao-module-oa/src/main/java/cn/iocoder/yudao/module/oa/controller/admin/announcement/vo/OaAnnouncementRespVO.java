package cn.iocoder.yudao.module.oa.controller.admin.announcement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - OA 公告 Response VO")
@Data
public class OaAnnouncementRespVO {

    @Schema(description = "公告编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "发布人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long publisherUserId;

    @Schema(description = "发布人用户昵称", example = "芋道源码")
    private String publisherUserName;

    @Schema(description = "发布人部门编号", example = "1")
    private Long publisherDeptId;

    @Schema(description = "发布人部门名称", example = "研发部")
    private String publisherDeptName;

    @Schema(description = "公告类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer priority;

    @Schema(description = "公告标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "国庆节放假通知")
    private String title;

    @Schema(description = "公告内容", example = "请各部门提前安排好假期值班工作。")
    private String content;

    @Schema(description = "相关链接", example = "https://example.com/file.pdf")
    private String url;

    @Schema(description = "是否置顶", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean top;

    @Schema(description = "接收人用户编号列表", example = "[1, 2]")
    private List<Long> receiverUserIds;

    @Schema(description = "接收人用户昵称列表", example = "[\"张三\", \"李四\"]")
    private List<String> receiverUserNames;

    @Schema(description = "当前接收人是否已读", example = "false")
    private Boolean readStatus;

    @Schema(description = "当前接收人是否已转发给下属", example = "false")
    private Boolean forwarded;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

}
