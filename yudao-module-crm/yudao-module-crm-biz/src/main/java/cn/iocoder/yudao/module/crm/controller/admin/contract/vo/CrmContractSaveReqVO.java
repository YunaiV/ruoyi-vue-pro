package cn.iocoder.yudao.module.crm.controller.admin.contract.vo;

import cn.iocoder.yudao.module.crm.framework.operatelog.core.CrmBusinessParseFunction;
import cn.iocoder.yudao.module.crm.framework.operatelog.core.CrmContactParseFunction;
import cn.iocoder.yudao.module.crm.framework.operatelog.core.CrmCustomerParseFunction;
import cn.iocoder.yudao.module.crm.framework.operatelog.core.SysAdminUserParseFunction;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - CRM 合同创建/更新 Request VO")
@Data
public class CrmContractSaveReqVO {

    @Schema(description = "合同编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10430")
    private Long id;

    @Schema(description = "合同名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @DiffLogField(name = "合同名称")
    @NotNull(message = "合同名称不能为空")
    private String name;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18336")
    @DiffLogField(name = "客户", function = CrmCustomerParseFunction.NAME)
    @NotNull(message = "客户编号不能为空")
    private Long customerId;

    @Schema(description = "商机编号", example = "10864")
    @DiffLogField(name = "商机", function = CrmBusinessParseFunction.NAME)
    private Long businessId;

    @Schema(description = "下单日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @DiffLogField(name = "下单日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @NotNull(message = "下单日期不能为空")
    private LocalDateTime orderDate;

    @Schema(description = "负责人的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17144")
    @DiffLogField(name = "负责人", function = SysAdminUserParseFunction.NAME)
    @NotNull(message = "负责人不能为空")
    private Long ownerUserId;

    // TODO @芋艿：未来应该支持自动生成；
    @Schema(description = "合同编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20230101")
    @DiffLogField(name = "合同编号")
    @NotNull(message = "合同编号不能为空")
    private String no;

    @Schema(description = "开始时间")
    @DiffLogField(name = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DiffLogField(name = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "合同金额", example = "5617")
    @DiffLogField(name = "合同金额")
    private Integer price;

    @Schema(description = "整单折扣")
    @DiffLogField(name = "整单折扣")
    private Integer discountPercent;

    @Schema(description = "产品总金额", example = "19510")
    @DiffLogField(name = "产品总金额")
    private Integer productPrice;

    @Schema(description = "联系人编号", example = "18546")
    @DiffLogField(name = "联系人", function = CrmContactParseFunction.NAME)
    private Long contactId;

    @Schema(description = "公司签约人", example = "14036")
    @DiffLogField(name = "公司签约人", function = SysAdminUserParseFunction.NAME)
    private Long signUserId;

    @Schema(description = "备注", example = "你猜")
    @DiffLogField(name = "备注")
    private String remark;


    @Schema(description = "产品列表")
    private List<CrmContractProductItem> productItems;

    @Schema(description = "产品列表")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrmContractProductItem {

        @Schema(description = "产品编号", example = "20529")
        @NotNull(message = "产品编号不能为空")
        private Long id;

        @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "8911")
        @NotNull(message = "产品数量不能为空")
        private Integer count;

        @Schema(description = "产品折扣")
        private Integer discountPercent;

    }

}
