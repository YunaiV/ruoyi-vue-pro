package cn.iocoder.yudao.module.iot.controller.admin.alert.vo.config;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - IoT 告警配置新增/修改 Request VO")
@Data
public class IotAlertConfigSaveReqVO {

    @Schema(description = "配置编号", example = "3566")
    private Long id;

    @Schema(description = "配置名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "配置名称不能为空")
    private String name;

    @Schema(description = "配置描述", example = "你猜")
    private String description;

    @Schema(description = "告警级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "告警级别不能为空")
    private Integer level;

    @Schema(description = "配置状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "配置状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "关联的场景联动规则编号数组")
    @NotEmpty(message = "关联的场景联动规则编号数组不能为空")
    private List<Long> sceneRuleIds;

    @Schema(description = "接收的用户编号数组")
    @NotEmpty(message = "接收的用户编号数组不能为空")
    private List<Long> receiveUserIds;

    @Schema(description = "接收的类型数组")
    @NotEmpty(message = "接收的类型数组不能为空")
    private List<Integer> receiveTypes;

}