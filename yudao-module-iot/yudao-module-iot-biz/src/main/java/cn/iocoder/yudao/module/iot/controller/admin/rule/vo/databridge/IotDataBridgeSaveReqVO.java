package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config.IotDataBridgeAbstractConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgeDirectionEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgeTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - IoT 数据桥梁新增/修改 Request VO")
@Data
public class IotDataBridgeSaveReqVO {

    @Schema(description = "桥梁编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18564")
    private Long id;

    @Schema(description = "桥梁名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "桥梁名称不能为空")
    private String name;

    @Schema(description = "桥梁描述", example = "随便")
    private String description;

    @Schema(description = "桥梁状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "桥梁状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "桥梁方向", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "桥梁方向不能为空")
    @InEnum(IotDataBridgeDirectionEnum.class)
    private Integer direction;

    @Schema(description = "桥梁类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "桥梁类型不能为空")
    @InEnum(IotDataBridgeTypeEnum.class)
    private Integer type;

    @Schema(description = "桥梁配置")
    @NotNull(message = "桥梁配置不能为空")
    private IotDataBridgeAbstractConfig config;

}