package cn.iocoder.yudao.module.iot.controller.admin.device.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStatusEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 设备分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotDevicePageReqVO extends PageParam {

    // TODO @芋艿：需要去掉一些多余的字段；

    @Schema(description = "设备唯一标识符", example = "24602")
    private String deviceKey;

    @Schema(description = "设备名称", example = "王五")
    private String deviceName;

    @Schema(description = "备注名称", example = "张三")
    private String nickname;

    @Schema(description = "产品编号", example = "26202")
    private Long productId;

    @Schema(description = "产品标识")
    private String productKey;

    @Schema(description = "设备类型", example = "1")
    @InEnum(IotProductDeviceTypeEnum.class)
    private Integer deviceType;

    @Schema(description = "网关设备 ID", example = "16380")
    private Long gatewayId;

    @Schema(description = "设备状态", example = "1")
    @InEnum(IotDeviceStatusEnum.class)
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

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}