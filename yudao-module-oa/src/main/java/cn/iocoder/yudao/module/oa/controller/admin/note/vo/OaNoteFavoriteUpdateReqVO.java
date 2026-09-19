package cn.iocoder.yudao.module.oa.controller.admin.note.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - OA 笔记收藏状态修改 Request VO")
@Data
public class OaNoteFavoriteUpdateReqVO {

    @Schema(description = "笔记编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "笔记编号不能为空")
    private Long id;

    @Schema(description = "是否收藏", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "收藏状态不能为空")
    private Boolean favorite;

}
