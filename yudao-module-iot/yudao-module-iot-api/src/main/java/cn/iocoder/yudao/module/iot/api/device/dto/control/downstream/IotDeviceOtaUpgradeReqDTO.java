package cn.iocoder.yudao.module.iot.api.device.dto.control.downstream;

import cn.hutool.core.map.MapUtil;
import lombok.Data;

import java.util.Map;

/**
 * IoT 设备【OTA】升级下发 Request DTO（更新固件消息）
 *
 * @author 芋道源码
 */
@Data
public class IotDeviceOtaUpgradeReqDTO extends IotDeviceDownstreamAbstractReqDTO {

    /**
     * 固件编号
     */
    private Long firmwareId;
    /**
     * 固件版本
     */
    private String version;

    /**
     * 签名方式
     *
     * 例如说：MD5、SHA256
     */
    private String signMethod;
    /**
     * 固件文件签名
     */
    private String fileSign;
    /**
     * 固件文件大小
     */
    private Long fileSize;
    /**
     * 固件文件 URL
     */
    private String fileUrl;

    /**
     * 自定义信息，建议使用 JSON 格式
     */
    private String information;

    public static IotDeviceOtaUpgradeReqDTO build(Map<?, ?> map) {
        return new IotDeviceOtaUpgradeReqDTO()
                .setFirmwareId(MapUtil.getLong(map, "firmwareId")).setVersion((String) map.get("version"))
                .setSignMethod((String) map.get("signMethod")).setFileSign((String) map.get("fileSign"))
                .setFileSize(MapUtil.getLong(map, "fileSize")).setFileUrl((String) map.get("fileUrl"))
                .setInformation((String) map.get("information"));
    }

    public static Map<?, ?> build(IotDeviceOtaUpgradeReqDTO dto) {
        return MapUtil.builder()
                .put("firmwareId", dto.getFirmwareId()).put("version", dto.getVersion())
                .put("signMethod", dto.getSignMethod()).put("fileSign", dto.getFileSign())
                .put("fileSize", dto.getFileSize()).put("fileUrl", dto.getFileUrl())
                .put("information", dto.getInformation())
                .build();
    }

}
