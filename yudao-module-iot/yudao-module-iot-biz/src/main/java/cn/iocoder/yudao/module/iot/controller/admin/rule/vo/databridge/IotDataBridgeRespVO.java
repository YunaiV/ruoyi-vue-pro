package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge;

import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config.IotDataBridgeAbstractConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 数据桥梁 Response VO")
@Data
public class IotDataBridgeRespVO {

    @Schema(description = "桥梁编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18564")
    private Long id;

    @Schema(description = "桥梁名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    private String name;

    @Schema(description = "桥梁描述", example = "随便")
    private String description;

    @Schema(description = "桥梁状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "桥梁方向", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer direction;

    @Schema(description = "桥梁类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "桥梁配置")
    private IotDataBridgeAbstractConfig config;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}