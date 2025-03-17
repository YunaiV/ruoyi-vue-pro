package cn.iocoder.yudao.module.iot.controller.admin.device.vo.device;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;

/**
 * 设备 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免设备导入有问题
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

}