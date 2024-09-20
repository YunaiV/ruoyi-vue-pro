package cn.iocoder.yudao.module.iot.controller.admin.device.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 设备分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotDevicePageReqVO extends PageParam {

    @Schema(description = "设备唯一标识符，全局唯一，用于识别设备")
    private String deviceKey;

    @Schema(description = "设备名称，在产品内唯一，用于标识设备", example = "王五")
    private String deviceName;

    @Schema(description = "产品 ID，关联 iot_product 表的 id", example = "26202")
    private Long productId;

    @Schema(description = "产品 Key，关联 iot_product 表的 product_key")
    private String productKey;

    @Schema(description = "设备类型：0 - 直连设备，1 - 网关子设备，2 - 网关设备", example = "1")
    private Integer deviceType;

    @Schema(description = "设备备注名称，供用户自定义备注", example = "张三")
    private String nickname;

    @Schema(description = "网关设备 ID，子设备需要关联的网关设备 ID", example = "16380")
    private Long gatewayId;

    @Schema(description = "设备状态：0 - 未激活，1 - 在线，2 - 离线，3 - 已禁用", example = "1")
    private Integer status;

    @Schema(description = "设备状态最后更新时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] statusLastUpdateTime;

    @Schema(description = "最后上线时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] lastOnlineTime;

    @Schema(description = "最后离线时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] lastOfflineTime;

    @Schema(description = "设备激活时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] activeTime;

    @Schema(description = "设备的 IP 地址")
    private String ip;

    @Schema(description = "设备的固件版本")
    private String firmwareVersion;

    @Schema(description = "设备密钥，用于设备认证，需安全存储")
    private String deviceSecret;

    @Schema(description = "MQTT 客户端 ID", example = "24602")
    private String mqttClientId;

    @Schema(description = "MQTT 用户名", example = "芋艿")
    private String mqttUsername;

    @Schema(description = "MQTT 密码")
    private String mqttPassword;

    @Schema(description = "认证类型（如一机一密、动态注册）", example = "2")
    private String authType;

    @Schema(description = "设备位置的纬度，范围 -90.000000 ~ 90.000000")
    private BigDecimal latitude;

    @Schema(description = "设备位置的经度，范围 -180.000000 ~ 180.000000")
    private BigDecimal longitude;

    @Schema(description = "地区编码，符合国家地区编码标准，关联地区表", example = "16995")
    private Integer areaId;

    @Schema(description = "设备详细地址")
    private String address;

    @Schema(description = "设备序列号")
    private String serialNumber;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}