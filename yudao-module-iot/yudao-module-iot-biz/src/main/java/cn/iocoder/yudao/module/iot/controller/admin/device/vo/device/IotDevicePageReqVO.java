package cn.iocoder.yudao.module.iot.controller.admin.device.vo.device;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备分页 Request VO")
@Data
public class IotDevicePageReqVO extends PageParam {

    @Schema(description = "设备名称", example = "王五")
    private String deviceName;

    @Schema(description = "备注名称", example = "张三")
    private String nickname;

    @Schema(description = "产品编号", example = "26202")
    private Long productId;

    @Schema(description = "设备类型", example = "1")
    @InEnum(IotProductDeviceTypeEnum.class)
    private Integer deviceType;

    @Schema(description = "设备状态", example = "1")
    @InEnum(IotDeviceStateEnum.class)
    private Integer status;

    @Schema(description = "设备分组编号", example = "1024")
    private Long groupId;

}