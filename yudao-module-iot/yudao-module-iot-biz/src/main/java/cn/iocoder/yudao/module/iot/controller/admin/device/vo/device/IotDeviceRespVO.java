package cn.iocoder.yudao.module.iot.controller.admin.device.vo.device;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.iot.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static cn.iocoder.yudao.module.iot.enums.DictTypeConstants.DEVICE_STATE;

@Schema(description = "管理后台 - IoT 设备 Response VO")
@Data
@ExcelIgnoreUnannotated
public class IotDeviceRespVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "177")
    private Long id;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("设备名称")
    private String deviceName;

    @Schema(description = "设备备注名称", example = "张三")
    @ExcelProperty("设备备注名称")
    private String nickname;

    @Schema(description = "设备序列号", example = "1024")
    @ExcelProperty("设备序列号")
    private String serialNumber;

    @Schema(description = "设备图片", example = "我是一名码农")
    @ExcelProperty("设备图片")
    private String picUrl;

    @Schema(description = "设备分组编号数组", example = "1,2")
    private Set<Long> groupIds;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26202")
    @ExcelProperty("产品编号")
    private Long productId;

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品 Key")
    private String productKey;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("设备类型")
    private Integer deviceType;

    @Schema(description = "网关设备 ID", example = "16380")
    private Long gatewayId;

    @Schema(description = "设备状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "设备状态", converter = DictConvert.class)
    @DictFormat(DEVICE_STATE)
    private Integer state;

    @Schema(description = "最后上线时间")
    @ExcelProperty("最后上线时间")
    private LocalDateTime onlineTime;

    @Schema(description = "最后离线时间")
    @ExcelProperty("最后离线时间")
    private LocalDateTime offlineTime;

    @Schema(description = "设备激活时间")
    @ExcelProperty("设备激活时间")
    private LocalDateTime activeTime;

    @Schema(description = "设备密钥，用于设备认证")
    @ExcelProperty("设备密钥")
    private String deviceSecret;

    @Schema(description = "认证类型（如一机一密、动态注册）", example = "2")
    @ExcelProperty("认证类型（如一机一密、动态注册）")
    private String authType;

    @Schema(description = "设备配置", example = "{\"abc\": \"efg\"}")
    private String config;

    @Schema(description = "定位方式", example = "2")
    @ExcelProperty(value = "定位方式", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.LOCATION_TYPE)
    private Integer locationType;

    @Schema(description = "设备位置的纬度", example = "45.000000")
    private BigDecimal latitude;

    @Schema(description = "设备位置的经度", example = "45.000000")
    private BigDecimal longitude;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}