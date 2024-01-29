package cn.iocoder.yudao.module.report.controller.admin.goview.vo.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - GoView 项目 Response VO")
@Data
public class GoViewProjectRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18993")
    private Long id;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    private String name;

    @Schema(description = "发布状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "报表内容") // JSON 格式
    private String content;

    @Schema(description = "预览图片 URL", example = "https://www.iocoder.cn")
    private String picUrl;

    @Schema(description = "项目备注", example = "你猜")
    private String remark;

    @Schema(description = "创建人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
