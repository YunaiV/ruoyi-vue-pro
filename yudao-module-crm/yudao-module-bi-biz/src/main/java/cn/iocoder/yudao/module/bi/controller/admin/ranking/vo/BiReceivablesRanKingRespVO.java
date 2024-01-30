package cn.iocoder.yudao.module.bi.controller.admin.ranking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// TODO @anhaohao：参考 BiContractRanKingRespVO 的建议
/**
 * 管理后台 - BI 排行榜 Response VO
 *
 * @author anhaohao
 */
@Schema(description = "管理后台 - BI 合同金额排行榜 Response VO")
@Data
public class BiReceivablesRanKingRespVO {

    @Schema(description = "金额", example = "100")
    private Integer price;

    @Schema(description = "姓名", example = "张三")
    private String nickname;

    @Schema(description = "部门名称", example = "研发部")
    private String deptName;

}
