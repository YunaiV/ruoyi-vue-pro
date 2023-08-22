package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup;

import cn.iocoder.yudao.framework.common.validation.Mobile;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;

@Schema(description = "管理后台 - 自提门店精简信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPickUpStoreSimpleRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23128")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotBlank(message = "门店名称不能为空")
    private String name;

    @Schema(description = "门店手机", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601892312")
    @NotBlank(message = "门店手机不能为空")
    @Mobile
    private String phone;

    @Schema(description = "区域编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18733")
    @NotNull(message = "区域编号不能为空")
    private Integer areaId;

    @Schema(description = "门店详细地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "复旦大学路 188 号")
    @NotBlank(message = "门店详细地址不能为空")
    private String detailAddress;

    @Schema(description = "营业开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "营业开始时间不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime openingTime;

    @Schema(description = "营业结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "营业结束时间不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime closingTime;

}
