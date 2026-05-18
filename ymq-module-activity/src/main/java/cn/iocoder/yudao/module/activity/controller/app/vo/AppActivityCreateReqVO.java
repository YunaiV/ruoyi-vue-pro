package cn.iocoder.yudao.module.activity.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "用户 App - 创建活动 Request VO")
@Data
public class AppActivityCreateReqVO {

    @Schema(description = "活动标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "周日双打局")
    @NotBlank(message = "活动标题不能为空")
    @Size(max = 128)
    private String title;

    @Schema(description = "场馆名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张家港全民健身中心")
    @NotBlank(message = "场馆名不能为空")
    @Size(max = 128)
    private String venueName;

    @Schema(description = "场馆地址", example = "张家港市人民东路 88 号")
    @Size(max = 255)
    private String venueAddress;

    private BigDecimal venueLat;
    private BigDecimal venueLng;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始时间不能为空")
    @Future(message = "开始时间必须是未来时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束时间不能为空")
    @Future
    private LocalDateTime endTime;

    @Schema(description = "场地数", example = "2")
    @Min(1) @Max(8)
    private Integer courtCount = 1;

    @Schema(description = "计划人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    @NotNull @Min(2) @Max(32)
    private Integer plannedCount;

    @Schema(description = "MEN_DOUBLES/WOMEN_DOUBLES/MIXED/FREE", example = "MIXED")
    private String mode = "MIXED";

    @Schema(description = "FIXED/ROTATE", example = "ROTATE")
    private String rotation = "ROTATE";

    @Schema(description = "单场分钟", example = "15")
    @Min(5) @Max(60)
    private Integer matchDuration = 15;

    @Schema(description = "AA 金额", example = "30")
    private BigDecimal aaAmount;

    @Schema(description = "备注", example = "自带水和拍子")
    @Size(max = 500)
    private String remark;

    /** 召集人 user id（D5 登录链路接通后改成从 SecurityContext 取，移除此字段） */
    @NotNull
    @Schema(description = "TODO D5：换成 SecurityContext", example = "1")
    private Long creatorId;

}
