package cn.iocoder.yudao.module.statistics.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 会员性别统计 Response VO")
@Data
public class MemberSexStatisticsRespVO {

    @Schema(description = "性别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sex;

    // TODO @疯狂：要不还是其它字段，我们也补全，这样方便使用的用户，做定制化；就保持和 MemberAreaStatisticsRespVO 一致；
    @Schema(description = "会员数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer userCount;

}
