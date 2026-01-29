package cn.iocoder.yudao.module.iot.core.biz.dto;

import cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterReqDTO;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * IoT 子设备动态注册 Request DTO
 * <p>
 * 额外包含了网关设备的标识信息
 *
 * @author 芋道源码
 */
@Data
public class IotSubDeviceRegisterFullReqDTO {

    /**
     * 网关设备 ProductKey
     */
    @NotEmpty(message = "网关产品标识不能为空")
    private String gatewayProductKey;

    /**
     * 网关设备 DeviceName
     */
    @NotEmpty(message = "网关设备名称不能为空")
    private String gatewayDeviceName;

    /**
     * 子设备注册列表
     */
    @NotNull(message = "子设备注册列表不能为空")
    private List<IotSubDeviceRegisterReqDTO> subDevices;

}
