package cn.iocoder.yudao.module.huawei.smarthome.dto.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("华为智能家居 - 设备基本信息 VO")
@Data
public class DevInfoVO {

    @ApiModelProperty(value = "设备型号", required = true, example = "MODEL_XYZ")
    private String model;

    @ApiModelProperty(value = "设备类型 ID", required = true, example = "TYPE_ABC")
    private String devType;

    @ApiModelProperty(value = "设备名称", example = "客厅空调")
    private String deviceName; // 文档中为可选

    @ApiModelProperty(value = "设备 SN 号", required = true, example = "SN123456789")
    private String sn;

    @ApiModelProperty(value = "制造商 ID", required = true, example = "MANU_ID")
    private String manu;

    @ApiModelProperty(value = "产品 ID", required = true, example = "PROD_ID")
    private String prodId;

    @ApiModelProperty(value = "SDK 版本", required = true, example = "1.0.0")
    private String hiv; // sdk 版本

    @ApiModelProperty(value = "软件版本", example = "2.0.1")
    @JsonProperty("swv") // 确保JSON字段名正确
    private String softwareVersion;

    @ApiModelProperty(value = "固件版本", example = "1.5.0")
    @JsonProperty("fwv")
    private String firmwareVersion;

    @ApiModelProperty(value = "硬件版本", example = "1.0")
    @JsonProperty("hwv")
    private String hardwareVersion;

    @ApiModelProperty(value = "协议类型", example = "3")
    private Integer protType; // 1: WiFi, 2: Z-Wave, 3: ZigBee, 4: BLE
}
