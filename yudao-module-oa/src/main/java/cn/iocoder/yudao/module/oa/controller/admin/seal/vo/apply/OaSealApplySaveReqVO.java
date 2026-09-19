package cn.iocoder.yudao.module.oa.controller.admin.seal.vo.apply;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.seal.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "管理后台 - 用印申请新增/修改 Request VO")
public class OaSealApplySaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "印章编号", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "印章不能为空")
    private Long sealId;

    @Schema(description = "用印事由", example = "合同签署用印", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用印事由不能为空")
    @Size(max = 500, message = "用印事由长度不能超过 {max} 个字符")
    private String reason;

    @Schema(description = "用印类型", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用印类型不能为空")
    @InEnum(value = OaSealApplyTypeEnum.class, message = "用印类型必须是 {value}")
    private Integer type;

    @Schema(description = "用印方式", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用印方式不能为空")
    @InEnum(value = OaSealUseModeEnum.class, message = "用印方式必须是 {value}")
    private Integer mode;

    @Schema(description = "文件标题", example = "采购合同")
    @Size(max = 255, message = "文件标题长度不能超过 {max} 个字符")
    private String documentTitle;

    @Schema(description = "文件类型", example = "合同")
    @Size(max = 64, message = "文件类型长度不能超过 {max} 个字符")
    private String documentType;

    @Schema(description = "文件份数", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件份数不能为空")
    @Min(value = 1, message = "文件份数不能小于 {value}")
    private Integer documentCount;

    @Schema(description = "合同金额", example = "10000.00")
    @DecimalMin(value = "0", message = "合同金额不能小于 {value}")
    @Digits(integer = 16, fraction = 2, message = "合同金额整数位不能超过 {integer} 位，小数位不能超过 {fraction} 位")
    private BigDecimal contractPrice;

    @Schema(description = "合同对方", example = "示例公司")
    @Size(max = 255, message = "合同对方长度不能超过 {max} 个字符")
    private String contractParty;

    @Schema(description = "预计用印时间", example = "1789088400000", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64")
    @NotNull(message = "预计用印时间不能为空")
    private LocalDateTime expectedUseTime;

    @Schema(description = "预计归还时间", example = "1789120800000", type = "integer", format = "int64")
    private LocalDateTime expectedReturnTime;

    @Schema(description = "实际归还时间", example = "1789117200000", type = "integer", format = "int64")
    private LocalDateTime actualReturnTime;

    @Schema(description = "是否紧急", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否紧急不能为空")
    private Boolean urgent;

    @Schema(description = "备注", example = "合同签署完成后归档")
    @Size(max = 500, message = "备注长度不能超过 {max} 个字符")
    private String remark;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/contract.pdf\"]")
    @Size(max = 5, message = "附件地址列表数量不能超过 {max} 个")
    private List<@NotBlank(message = "附件地址不能为空") @Size(max = 2048, message = "附件地址长度不能超过 {max} 个字符") String> fileUrls;
}
