package cn.iocoder.yudao.module.fms.controller.admin.voucher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - FMS 凭证 Response VO")
@Data
public class FmsVoucherRespVO {

    @Schema(description = "凭证编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long accountSetId;

    @Schema(description = "凭证字编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long voucherWordId;

    @Schema(description = "凭证字", requiredMode = Schema.RequiredMode.REQUIRED)
    private String voucherWordName;

    @Schema(description = "凭证号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer voucherNumber;

    @Schema(description = "凭证日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime voucherTime;

    @Schema(description = "附件地址数组")
    private List<String> attachmentUrls;

    @Schema(description = "附件数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer attachmentCount;

    @Schema(description = "借方金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal debitAmount;

    @Schema(description = "贷方金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal creditAmount;

    @Schema(description = "合计金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal total;

    @Schema(description = "审核状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "是否为结账生成凭证", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean closingGenerated;

    @Schema(description = "制单人后台用户编号")
    private Long creatorUserId;

    @Schema(description = "制单人名称")
    private String creatorUserName;

    @Schema(description = "审核人后台用户编号")
    private Long reviewerUserId;

    @Schema(description = "审核人名称")
    private String reviewerUserName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "凭证分录数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Entry> entries;

    @Schema(description = "管理后台 - FMS 凭证分录 Response VO")
    @Data
    public static class Entry {

        @Schema(description = "分录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "摘要内容", requiredMode = Schema.RequiredMode.REQUIRED)
        private String digest;

        @Schema(description = "科目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long subjectId;

        @Schema(description = "科目编码", requiredMode = Schema.RequiredMode.REQUIRED)
        private String subjectCode;

        @Schema(description = "科目名称", requiredMode = Schema.RequiredMode.REQUIRED)
        private String subjectName;

        @Schema(description = "数量")
        private BigDecimal quantity;

        @Schema(description = "单价")
        private BigDecimal unitPrice;

        @Schema(description = "借方金额", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal debitAmount;

        @Schema(description = "贷方金额", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal creditAmount;

        @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer sort;

        @Schema(description = "辅助核算组合编号")
        private Long assistCombinationId;

        @Schema(description = "辅助核算项目数组")
        private List<AuxiliaryItem> auxiliaries;

        @Schema(description = "管理后台 - FMS 凭证分录辅助核算项目")
        @Data
        public static class AuxiliaryItem {

            @Schema(description = "辅助核算类型", requiredMode = Schema.RequiredMode.REQUIRED)
            private Integer type;

            @Schema(description = "辅助核算类别编号", requiredMode = Schema.RequiredMode.REQUIRED)
            private Long typeId;

            @Schema(description = "辅助核算项目编号", requiredMode = Schema.RequiredMode.REQUIRED)
            private Long itemId;

            @Schema(description = "辅助核算项目名称", requiredMode = Schema.RequiredMode.REQUIRED)
            private String name;
        }
    }

}
