package cn.iocoder.yudao.module.bi.controller.admin.ranking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 排行榜 Request VO
 *
 * @author anhaohao
 */
@Schema(description = "管理后台 - 排行榜 Request VO")
@Data
public class BiRankReqVO {

    @Schema(description = "部门id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long deptId;

    // TODO @anhaohao：这个字段，参考 BiParams 的 type 建议
    @Schema(description = "分析类型(1.今天 2.昨天 3.本周 4.上周 5.本月 6.上月 7.本季度 8.上季度 9.本年 10 上年)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String type;

}
