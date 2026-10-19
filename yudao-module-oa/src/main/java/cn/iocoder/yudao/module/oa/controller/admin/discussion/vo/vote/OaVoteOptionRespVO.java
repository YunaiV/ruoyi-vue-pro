package cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.vote;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - OA 投票选项 Response VO")
@Data
public class OaVoteOptionRespVO {

    @Schema(description = "投票选项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "选项标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "周五下午")
    private String title;

    @Schema(description = "选项颜色", example = "#409EFF")
    private String color;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer sort;

    @Schema(description = "投票数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer voteCount;

    @Schema(description = "当前用户是否已选择", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean voted;

    @Schema(description = "投票人用户昵称列表", example = "[\"张三\", \"李四\"]")
    private List<String> voterUserNames;

}
