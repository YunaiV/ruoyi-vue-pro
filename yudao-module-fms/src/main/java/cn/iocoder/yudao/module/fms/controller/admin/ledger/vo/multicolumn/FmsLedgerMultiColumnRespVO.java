package cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.multicolumn;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.detail.FmsLedgerDetailRespVO;

import java.util.List;

@Schema(description = "管理后台 - FMS 多栏账 Response VO")
@Data
@Accessors(chain = true)
public class FmsLedgerMultiColumnRespVO {

    @Schema(description = "动态科目列")
    private List<Column> columns;

    @Schema(description = "账簿行")
    private List<FmsLedgerDetailRespVO> rows;

    @Schema(description = "管理后台 - FMS 多栏账科目列 Response VO")
    @Data
    public static class Column {

        @Schema(description = "科目编号", example = "1024")
        private Long subjectId;

        @Schema(description = "科目编码", example = "560101")
        private String subjectCode;

        @Schema(description = "科目名称", example = "办公用品")
        private String subjectName;

        @Schema(description = "余额方向", example = "1")
        private Integer balanceDirection;

    }

}
