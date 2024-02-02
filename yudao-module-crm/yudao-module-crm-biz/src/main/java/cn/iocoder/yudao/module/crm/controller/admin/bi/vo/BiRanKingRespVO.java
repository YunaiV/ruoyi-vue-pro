package cn.iocoder.yudao.module.crm.controller.admin.bi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "管理后台 - BI 排行榜 Response VO")
@Data
public class BiRanKingRespVO {

    @Schema(description = "金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer price;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String nickname;

    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String deptName;

    @Schema(description = "负责人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long ownerUserId;

}
