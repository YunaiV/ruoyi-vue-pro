package cn.iocoder.yudao.module.im.controller.admin.group.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

// TODO @AI：去掉部分不需要的 save 字段；
@Schema(description = "管理后台 - 群新增/修改 Request VO")
@Data
public class ImGroupSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1003")
    private Long id;

    @Schema(description = "群名称", example = "芋艿")
    private String name;

    @Schema(description = "群主用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31460")
    @NotNull(message = "群主用户编号不能为空")
    private Long ownerUserId;

    @Schema(description = "群头像")
    private String avatar;

    @Schema(description = "群公告")
    private String notice;

    @Schema(description = "是否封禁")
    private Boolean banned;

    @Schema(description = "封禁原因")
    private String bannedReason;

    @Schema(description = "是否解散")
    private Boolean dissolved;

}