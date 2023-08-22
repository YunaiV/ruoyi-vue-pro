package cn.iocoder.yudao.module.member.controller.admin.group.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Schema(description = "管理后台 - 用户分组 Response VO")
@Data
@ToString(callSuper = true)
public class MemberGroupSimpleRespVO {

    @Schema(description = "编号", example = "6103")
    private Long id;

    @Schema(description = "等级名称", example = "芋艿")
    private String name;

}
