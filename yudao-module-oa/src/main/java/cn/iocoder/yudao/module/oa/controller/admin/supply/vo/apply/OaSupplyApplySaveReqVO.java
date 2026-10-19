package cn.iocoder.yudao.module.oa.controller.admin.supply.vo.apply;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.supply.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 用品领用申请新增/修改 Request VO")
@Data
public class OaSupplyApplySaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "领用日期", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    @NotNull(message = "领用日期不能为空")
    private LocalDateTime applyTime;

    @Schema(description = "使用类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "使用类型不能为空")
    @InEnum(value = OaSupplyUseTypeEnum.class, message = "使用类型必须是 {value}")
    private Integer useType;

    @Schema(description = "领取方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "领取方式不能为空")
    @Min(value = 1, message = "领取方式不能小于 {value}")
    private Integer pickupMethod;

    @Schema(description = "申请事由", requiredMode = Schema.RequiredMode.REQUIRED, example = "领用部门日常办公用品")
    @NotBlank(message = "申请事由不能为空")
    @Size(max = 500, message = "申请事由长度不能超过 {max} 个字符")
    private String reason;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    @Size(max = 10, message = "附件地址列表数量不能超过 {max} 个")
    private List<@NotBlank(message = "附件地址不能为空") String> fileUrls;

    @Schema(description = "备注", example = "请提前联系行政部确认")
    @Size(max = 500, message = "备注长度不能超过 {max} 个字符")
    private String remark;

    @Schema(description = "领用明细", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "领用明细不能为空")
    @Valid
    private List<@NotNull(message = "领用明细不能为空") Item> items;

    @Schema(description = "管理后台 - 用品申请明细 Request VO")
    @Data
    public static class Item {

        @Schema(description = "办公用品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        @NotNull(message = "办公用品不能为空")
        private Long itemId;

        @Schema(description = "领用数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "领用数量不能为空")
        @Min(value = 1, message = "领用数量不能小于 1")
        private Integer applyQuantity;

    }

}
