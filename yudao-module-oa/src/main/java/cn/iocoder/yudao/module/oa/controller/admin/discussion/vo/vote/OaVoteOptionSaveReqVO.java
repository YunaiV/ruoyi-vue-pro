package cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.vote;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - OA 投票选项新增/修改 Request VO")
@Data
public class OaVoteOptionSaveReqVO {

    @Schema(description = "选项标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "周五下午")
    @NotBlank(message = "投票选项标题不能为空")
    @Size(max = 200, message = "投票选项标题长度不能超过 200 个字符")
    private String title;

    @Schema(description = "选项颜色", example = "#409EFF")
    @Size(max = 20, message = "投票选项颜色长度不能超过 20 个字符")
    private String color;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "显示顺序不能为空")
    private Integer sort;

}
