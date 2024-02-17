package cn.iocoder.yudao.module.crm.controller.admin.contract.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - CRM 合同 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CrmContractRespVO {

    @Schema(description = "合同编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10430")
    @ExcelProperty("合同编号")
    private Long id;

    @Schema(description = "合同名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("合同名称")
    private String name;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18336")
    @ExcelProperty("客户编号")
    private Long customerId;
    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "18336")
    @ExcelProperty("客户名称")
    private String customerName;

    @Schema(description = "商机编号", example = "10864")
    @ExcelProperty("商机编号")
    private Long businessId;
    @Schema(description = "商机名称", example = "10864")
    @ExcelProperty("商机名称")
    private String businessName;

    @Schema(description = "工作流编号", example = "1043")
    @ExcelProperty("工作流编号")
    private Long processInstanceId;

    @Schema(description = "下单日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("下单日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime orderDate;

    @Schema(description = "负责人的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17144")
    @ExcelProperty("负责人的用户编号")
    private Long ownerUserId;

    // TODO @芋艿：未来应该支持自动生成；
    @Schema(description = "合同编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20230101")
    @ExcelProperty("合同编号")
    private String no;

    @Schema(description = "开始时间")
    @ExcelProperty("开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @ExcelProperty("结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "合同金额", example = "5617")
    @ExcelProperty("合同金额")
    private Integer price;

    @Schema(description = "整单折扣")
    @ExcelProperty("整单折扣")
    private Integer discountPercent;

    @Schema(description = "产品总金额", example = "19510")
    @ExcelProperty("产品总金额")
    private Integer productPrice;

    @Schema(description = "联系人编号", example = "18546")
    @ExcelProperty("联系人编号")
    private Long contactId;
    @Schema(description = "联系人编号", example = "18546")
    @ExcelProperty("联系人编号")
    private String contactName;

    @Schema(description = "公司签约人", example = "14036")
    @ExcelProperty("公司签约人")
    private Long signUserId;
    @Schema(description = "公司签约人", example = "14036")
    @ExcelProperty("公司签约人")
    private String signUserName;

    @Schema(description = "最后跟进时间")
    @ExcelProperty("最后跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime contactLastTime;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTime;

    @Schema(description = "创建人", example = "25682")
    @ExcelProperty("创建人")
    private String creator;

    @Schema(description = "创建人名字", example = "test")
    @ExcelProperty("创建人名字")
    private String creatorName;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("更新时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime updateTime;

    @Schema(description = "负责人", example = "test")
    @ExcelProperty("负责人")
    private String ownerUserName;

    @Schema(description = "审批状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("审批状态")
    private Integer auditStatus;

    @Schema(description = "产品列表")
    private List<CrmContractProductItemRespVO> productItems;

    // TODO @puhui999：可以直接叫 Item
    @Schema(description = "产品列表")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrmContractProductItemRespVO {

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20529")
        private Long id;

        @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是产品")
        private String name;

        @Schema(description = "产品编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "N881")
        private String no;

        @Schema(description = "单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        private Integer unit;

        @Schema(description = "价格，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Integer price;

        @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
        private Integer count;

        @Schema(description = "产品折扣", example = "99")
        private Integer discountPercent;

    }

}
