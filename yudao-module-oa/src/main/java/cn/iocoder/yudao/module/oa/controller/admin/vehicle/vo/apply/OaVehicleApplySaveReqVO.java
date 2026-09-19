package cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.apply;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 用车申请新增/修改 Request VO")
@Data
public class OaVehicleApplySaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "车辆编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "车辆不能为空")
    private Long vehicleId;

    @Schema(description = "预计出车时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    @NotNull(message = "预计出车时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "预计回车时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789380000000")
    @NotNull(message = "预计回车时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "出车地点", requiredMode = Schema.RequiredMode.REQUIRED, example = "公司停车场")
    @NotBlank(message = "出车地点不能为空")
    @Size(max = 255, message = "出车地点长度不能超过 {max} 个字符")
    private String startLocation;

    @Schema(description = "预计回车地点", requiredMode = Schema.RequiredMode.REQUIRED, example = "公司停车场")
    @NotBlank(message = "预计回车地点不能为空")
    @Size(max = 255, message = "预计回车地点长度不能超过 {max} 个字符")
    private String endLocation;

    @Schema(description = "用车事由", requiredMode = Schema.RequiredMode.REQUIRED, example = "前往客户现场参加项目评审")
    @NotBlank(message = "用车事由不能为空")
    @Size(max = 500, message = "用车事由长度不能超过 {max} 个字符")
    private String reason;

    @Schema(description = "随行人", example = "张三、李四")
    @Size(max = 500, message = "随行人长度不能超过 500 个字符")
    private String passenger;

    @Schema(description = "备注", example = "请提前联系行政部确认")
    @Size(max = 500, message = "备注长度不能超过 {max} 个字符")
    private String remark;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    @Size(max = 5, message = "附件最多上传 5 个")
    private List<@NotBlank(message = "附件地址不能为空") @Size(max = 2048, message = "附件地址长度不能超过 {max} 个字符") String> fileUrls;

}
