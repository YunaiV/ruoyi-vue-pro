package cn.iocoder.yudao.module.iot.core.topic.ota;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备 OTA 升级进度上报 Request DTO
 * <p>
 * 用于 {@link IotDeviceMessageMethodEnum#OTA_PROGRESS} 上行消息的 params 参数
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/perform-ota-updates">阿里云 - OTA 升级</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceOtaProgressReqDTO {

    /**
     * 固件版本号
     */
    private String version;

    /**
     * 升级状态
     */
    private Integer status;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 升级进度（0-100）
     */
    private Integer progress;

}
