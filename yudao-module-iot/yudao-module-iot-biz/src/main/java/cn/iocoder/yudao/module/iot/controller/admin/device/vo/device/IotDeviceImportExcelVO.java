package cn.iocoder.yudao.module.iot.controller.admin.device.vo.device;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotLocationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IotDeviceImportExcelVO {

    @ExcelProperty("设备名称")
    @NotEmpty(message = "设备名称不能为空")
    private String deviceName;

    @ExcelProperty("父设备名称")
    @Schema(description = "父设备名称", example = "网关001")
    private String parentDeviceName;

    @ExcelProperty("产品标识")
    @NotEmpty(message = "产品标识不能为空")
    private String productKey;

    @ExcelProperty("设备分组")
    private String groupNames;

    @ExcelProperty("上报方式(1:IP 定位， 2:设备上报，3:手动定位)")
    @NotNull(message = "上报方式不能为空")
    @InEnum(IotLocationTypeEnum.class)
    private Integer locationType;

}
