package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - IoT 场景联动新增/修改 Request VO")
@Data
public class IotSceneRuleSaveReqVO {

    @Schema(description = "场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15865")
    private Long id;

    @Schema(description = "场景名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "场景名称不能为空")
    private String name;

    @Schema(description = "场景描述", example = "你猜")
    private String description;

    @Schema(description = "场景状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "场景状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "触发器数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "触发器数组不能为空")
    private List<IotSceneRuleDO.Trigger> triggers;

    @Schema(description = "执行器数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "执行器数组不能为空")
    private List<IotSceneRuleDO.Action> actions;

}