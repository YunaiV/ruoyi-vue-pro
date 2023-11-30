package cn.iocoder.yudao.module.crm.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 合同 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CrmContractBaseVO {

    // TODO @dhb52：类似 no 字段的 example 要写xia 哈；

    @Schema(description = "合同名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotNull(message = "合同名称不能为空")
    private String name;

    // TODO @dhb52：这个必须传递
    @Schema(description = "客户编号", example = "18336")
    private Long customerId;

    @Schema(description = "商机编号", example = "10864")
    private Long businessId;

    @Schema(description = "工作流编号", example = "1043")
    private Long processInstanceId;

    // TODO @dhb52：这个必须传递
    @Schema(description = "下单日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime orderDate;

    // TODO @dhb52：这个必须传递
    @Schema(description = "负责人的用户编号", example = "17144")
    private Long ownerUserId;

    // TODO @芋艿：未来应该支持自动生成；
    // TODO @dhb52：这个必须传递；
    @Schema(description = "合同编号")
    private String no;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "合同金额", example = "5617")
    private Integer price;

    @Schema(description = "整单折扣")
    private Integer discountPercent;

    @Schema(description = "产品总金额", example = "19510")
    private Integer productPrice;

    @Schema(description = "联系人编号", example = "18546")
    private Long contactId;

    @Schema(description = "公司签约人", example = "14036")
    private Long signUserId;

    @Schema(description = "最后跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime contactLastTime;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    // TODO @dhb52：增加一个 status 字段：具体有哪些值，你来枚举下；主要页面上有个【草稿】【提交审核】的流程，可以看看。然后要对接工作流，这块也可以看看，不确定的地方问我。

}
