package cn.iocoder.yudao.module.huawei.smarthome.dto.scenario;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("华为智能家居 - 场景信息 VO")
@Data
public class ScenarioVO {

    @ApiModelProperty(value = "场景 ID", required = true, example = "xxx1")
    private String scenarioId;

    @ApiModelProperty(value = "场景名", required = true, example = "回家模式")
    private String scenarioName;

    @ApiModelProperty(value = "房间 ID", required = true, example = "room-uuid-123")
    private String roomId; // 文档中是roomId，但示例中是roomId

    @ApiModelProperty(value = "类型", example = "1", notes = "枚举: 1 手动, 2 自动, 3 混合")
    private Integer type;

    @ApiModelProperty(value = "是否可以编辑", example = "true")
    private Boolean editAble; // 文档中为 editAble

    @ApiModelProperty(value = "状态", example = "1", notes = "枚举: 0 inactive, 1 active")
    private Integer status;
}
