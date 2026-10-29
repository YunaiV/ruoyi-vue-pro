package cn.iocoder.yudao.module.iot.controller.admin.device.vo.shadow;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理后台 - IoT 设备影子更新状态 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IoT 设备影子更新状态 Request VO")
@Data
public class IotDeviceShadowUpdateStatusReqVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
