package cn.iocoder.yudao.module.iot.core.topic.ota;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备 OTA 固件升级推送 Request DTO
 * <p>
 * 用于 {@link IotDeviceMessageMethodEnum#OTA_UPGRADE} 下行消息的 params 参数
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/perform-ota-updates">阿里云 - OTA 升级</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceOtaUpgradeReqDTO {

    /**
     * 固件版本号
     */
    private String version;

    /**
     * 固件文件下载地址
     */
    private String fileUrl;

    /**
     * 固件文件大小（字节）
     */
    private Long fileSize;

    /**
     * 固件文件摘要算法
     */
    private String fileDigestAlgorithm;

    /**
     * 固件文件摘要值
     */
    private String fileDigestValue;

}
