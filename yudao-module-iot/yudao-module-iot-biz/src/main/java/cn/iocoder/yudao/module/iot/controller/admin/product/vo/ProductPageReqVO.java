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

    @Schema(description = "产品标识")
    private String identification;

    @Schema(description = "设备类型：device、gatway、gatway_sub", example = "1")
    private String deviceType;

    @Schema(description = "厂商名称", example = "李四")
    private String manufacturerName;

    @Schema(description = "产品型号")
    private String model;

    @Schema(description = "数据格式:1. 标准数据格式（JSON）2. 透传/自定义，脚本解析")
    private Integer dataFormat;

    @Schema(description = "设备接入平台的协议类型，默认为MQTT", example = "2")
    private String protocolType;

    @Schema(description = "产品描述", example = "随便")
    private String description;

    @Schema(description = "产品状态 (0: 启用, 1: 停用)", example = "2")
    private Integer status;

    @Schema(description = "物模型定义")
    private String metadata;

    @Schema(description = "消息协议ID")
    private Long messageProtocol;

    @Schema(description = "消息协议名称", example = "芋艿")
    private String protocolName;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}