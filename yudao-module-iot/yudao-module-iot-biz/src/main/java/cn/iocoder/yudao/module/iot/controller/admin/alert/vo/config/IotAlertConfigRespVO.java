package cn.iocoder.yudao.module.iot.controller.admin.alert.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - IoT 告警配置 Response VO")
@Data
public class IotAlertConfigRespVO {

    @Schema(description = "配置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3566")
    private Long id;

    @Schema(description = "配置名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    private String name;

    @Schema(description = "配置描述", example = "你猜")
    private String description;

    @Schema(description = "告警级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer level;

    @Schema(description = "配置状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "关联的场景联动规则编号数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "1,2,3")
    private List<Long> sceneRuleIds;

    @Schema(description = "接收的用户编号数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "100,200")
    private List<Long> receiveUserIds;

    @Schema(description = "接收的用户名称数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三,李四")
    private List<String> receiveUserNames;

    @Schema(description = "接收的类型数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "1,2,3")
    private List<Integer> receiveTypes;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}