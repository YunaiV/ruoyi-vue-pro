package cn.iocoder.yudao.module.iot.controller.admin.device.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 设备 Response VO")
@Data
@ExcelIgnoreUnannotated
public class IotDeviceRespVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "177")
    private Long id;

    @Schema(description = "设备唯一标识符", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("设备唯一标识符")
    private String deviceKey;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("设备名称备")
    private String deviceName;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26202")
    @ExcelProperty("产品编号")
    private Long productId;

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品 Key")
    private String productKey;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("设备类型")
    private Integer deviceType;

    @Schema(description = "设备备注名称", example = "张三")
    @ExcelProperty("设备备注名称")
    private String nickname;

    @Schema(description = "网关设备 ID", example = "16380")
    private Long gatewayId;

    @Schema(description = "设备状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("设备状态")
    private Integer status;

    @Schema(description = "设备状态最后更新时间")
    @ExcelProperty("设备状态最后更新时间")
    private LocalDateTime statusLastUpdateTime;

    @Schema(description = "最后上线时间")
    @ExcelProperty("最后上线时间")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "最后离线时间")
    @ExcelProperty("最后离线时间")
    private LocalDateTime lastOfflineTime;

    @Schema(description = "设备激活时间")
    @ExcelProperty("设备激活时间")
    private LocalDateTime activeTime;

    @Schema(description = "设备密钥，用于设备认证")
    @ExcelProperty("设备密钥")
    private String deviceSecret;

    @Schema(description = "MQTT 客户端 ID", example = "24602")
    @ExcelProperty("MQTT 客户端 ID")
    private String mqttClientId;

    @Schema(description = "MQTT 用户名", example = "芋艿")
    @ExcelProperty("MQTT 用户名")
    private String mqttUsername;

    @Schema(description = "MQTT 密码")
    @ExcelProperty("MQTT 密码")
    private String mqttPassword;

    @Schema(description = "认证类型（如一机一密、动态注册）", example = "2")
    @ExcelProperty("认证类型（如一机一密、动态注册）")
    private String authType;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}