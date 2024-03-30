package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - CRM 客户级别分析 VO")
@Data
public class CrmStatisticCustomerLevelRespVO {

    @Schema(description = "客户级别编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer level;
    // TODO @puhui999：这个前端字典翻译哈
    @Schema(description = "客户级别名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private String levelName;

    @Schema(description = "客户个数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer customerCount;

    @Schema(description = "成交个数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer dealCount;

    // TODO @puhui999：下面两个的计算，交给前端。后端只返回数据即可。

    @Schema(description = "级别占比(%)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Double levelPortion;

    @Schema(description = "成交占比(%)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Double dealPortion;

}
