package cn.iocoder.yudao.module.member.controller.admin.level.vo.level;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Schema(description = "管理后台 - 会员等级列表筛选 Request VO")
@Data
@ToString(callSuper = true)
public class MemberLevelListReqVO {

    @Schema(description = "等级名称", example = "芋艿")
    private String name;

    @Schema(description = "状态", example = "1")
    private Integer status;

}
