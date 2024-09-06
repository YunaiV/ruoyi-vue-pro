package cn.iocoder.yudao.module.iot.controller.admin.product.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - iot 产品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPageReqVO extends PageParam {

    @Schema(description = "产品名称", example = "李四")
    private String name;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "产品标识")
    private String productKey;

    @Schema(description = "协议编号（脚本解析 id）", example = "13177")
    private Long protocolId;

    @Schema(description = "产品所属品类标识符", example = "14237")
    private Long categoryId;

    @Schema(description = "产品描述", example = "你猜")
    private String description;

    @Schema(description = "数据校验级别, 0: 强校验, 1: 弱校验, 2: 免校验", example = "1")
    private Integer validateType;

    @Schema(description = "产品状态, 0: DEVELOPMENT_STATUS, 1: RELEASE_STATUS", example = "1")
    private Integer status;

    @Schema(description = "设备类型, 0: 直连设备, 1: 网关子设备, 2: 网关设备", example = "2")
    private Integer deviceType;

    @Schema(description = "联网方式, 0: Wi-Fi, 1: Cellular, 2: Ethernet, 3: 其他", example = "2")
    private Integer netType;

    @Schema(description = "接入网关协议, 0: modbus, 1: opc-ua, 2: customize, 3: ble, 4: zigbee", example = "2")
    private Integer protocolType;

    @Schema(description = "数据格式, 0: 透传模式, 1: Alink JSON")
    private Integer dataFormat;

}