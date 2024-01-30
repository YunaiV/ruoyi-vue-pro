package cn.iocoder.yudao.module.bi.controller.admin.ranking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// TODO @anhaohao：VO 类有 swagger 注解，不写注释哈
/**
 * 管理后台 - BI 排行榜 Response VO
 *
 * @author anhaohao
 */
@Schema(description = "管理后台 - BI 合同金额排行榜 Response VO")
@Data
public class BiContractRanKingRespVO {

    // TODO @anhaohao：如果一定返回的字段，需要加 requiredMode = Schema.RequiredMode.REQUIRED, 哈
    @Schema(description = "金额", example = "1")
    private Integer price;

    @Schema(description = "姓名", example = "1")
    private String nickname;

    @Schema(description = "部门名称", example = "1")
    private String deptName;

}
