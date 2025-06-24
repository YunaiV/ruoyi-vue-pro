package cn.iocoder.yudao.module.huawei.smarthome.dto.scenario;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Map;

@ApiModel("华为智能家居 - 修改场景请求 DTO")
@Data
public class ScenarioUpdateReqDTO {

    @ApiModelProperty(value = "空间 ID", required = true, example = "space-uuid-123")
    @NotEmpty(message = "空间 ID 不能为空")
    @Size(min = 1, max = 24, message = "空间 ID 长度必须在 1-24 之间")
    private String spaceId; // 路径参数

    @ApiModelProperty(value = "场景 ID", required = true, example = "scene-uuid-456")
    @NotEmpty(message = "场景 ID 不能为空")
    @Size(min = 24, max = 256, message = "场景 ID 长度必须在 24-256 之间") // 根据文档调整
    private String scenarioId;

    @ApiModelProperty(value = "状态", example = "0", notes = "枚举: 0 inactive, 1 active (只有自动场景支持)")
    private Integer status;

    @ApiModelProperty(value = "自定义参数 (Key-value 键值对)", example = "{\"repeatTimes\": 10, \"repeatInterval\": 5}")
    private Map<String, Object> datas; // 对应文档中的 datas
}
