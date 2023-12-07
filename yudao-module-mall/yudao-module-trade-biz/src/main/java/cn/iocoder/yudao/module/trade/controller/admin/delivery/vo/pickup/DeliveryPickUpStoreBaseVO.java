package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

/**
* 自提门店 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class DeliveryPickUpStoreBaseVO {

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotBlank(message = "门店名称不能为空")
    private String name;

    @Schema(description = "门店简介", example = "我是门店简介")
    private String introduction;

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

    @Schema(description = "门店 logo", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.png")
    @NotBlank(message = "门店 logo 不能为空")
    private String logo;

    @Schema(description = "营业开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "营业开始时间不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime openingTime;

    @Schema(description = "营业结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "营业结束时间不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime closingTime;

    @Schema(description = "纬度", requiredMode = Schema.RequiredMode.REQUIRED, example = "5.88")
    @NotNull(message = "纬度不能为空")
    private Double latitude;

    @Schema(description = "经度", requiredMode = Schema.RequiredMode.REQUIRED, example = "6.99")
    @NotNull(message = "经度不能为空")
    private Double longitude;

    @Schema(description = "门店状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "门店状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
