package cn.iocoder.yudao.module.oa.controller.admin.travel.vo.reimbursement;

import cn.iocoder.yudao.framework.dict.validation.InDict;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 出差报销新增/修改 Request VO")
@Data
public class OaTravelReimbursementSaveReqVO {

    @Schema(description = "编号，新建时不传", example = "1024")
    private Long id;

    @Schema(description = "出差事由", requiredMode = Schema.RequiredMode.REQUIRED, example = "赴北京参加客户项目验收")
    @NotBlank(message = "出差事由不能为空")
    private String reason;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    @NotNull(message = "开始日期不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789380000000")
    @NotNull(message = "结束日期不能为空")
    private LocalDateTime endTime;

    @Schema(description = "关联出差申请编号", example = "1024")
    private Long travelApplyId;

    @Schema(description = "备注", example = "请提前联系行政部确认")
    private String remark;

    @Schema(description = "费用明细", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "请添加有效明细")
    private List<@NotNull(message = "明细不能为空") @Valid Item> items;

    @Schema(description = "附件地址列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"https://example.com/file.pdf\"]")
    @NotEmpty(message = "请上传报销附件")
    private List<@NotBlank(message = "附件地址不能为空") String> fileUrls;

    @AssertTrue(message = "结束日期不能早于开始日期")
    @JsonIgnore
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null
                || !endTime.isBefore(startTime);
    }

    @Schema(description = "管理后台 - 出差报销明细 Request VO")
    @Data
    public static class Item {

        @Schema(description = "费用类型", example = "1")
        @InDict(type = DictTypeConstants.EXPENSE_TYPE, message = "费用类型必须是 {value}")
        private Integer expenseType;

        @Schema(description = "发生日期", type = "integer", format = "int64", example = "1789347600000")
        private LocalDateTime expenseTime;

        @Schema(description = "出发地", example = "上海")
        private String departureCity;

        @Schema(description = "到达地", example = "北京")
        private String arrivalCity;

        @Schema(description = "金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "费用明细金额不能为空")
        @DecimalMin(value = "0", message = "金额不能小于零")
        private BigDecimal price;

        @Schema(description = "费用说明", example = "客户项目验收往返交通费")
        private String description;

    }

}
