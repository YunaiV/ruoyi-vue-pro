package cn.iocoder.yudao.module.huawei.smarthome.dto.scenario;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("华为智能家居 - 单个场景统计详情 VO")
@Data
public class ScenarioStatisticDetailVO {

    @ApiModelProperty(value = "场景名称", required = true, example = "回家模式")
    private String scenarioName;

    @ApiModelProperty(value = "场景 ID", required = true, example = "1586904738436374528")
    private String scenarioId;

    @ApiModelProperty(value = "场景房间 ID", required = true, example = "10406921")
    @JsonProperty("roomID") // 注意华为文档大小写
    private String roomId;

    @ApiModelProperty(value = "触发事件个数", required = true, example = "2")
    private Integer eventCount;

    @ApiModelProperty(value = "事件事件上的判断条件个数", required = true, example = "0")
    @JsonProperty("c-eventCount") // 注意华为文档字段名
    private Integer cEventCount;

    @ApiModelProperty(value = "执行动作个数", required = true, example = "27")
    private Integer actionCount;

    @ApiModelProperty(value = "执行动作上的判断条件个数", required = true, example = "4")
    @JsonProperty("c-actionCount") // 注意华为文档字段名
    private Integer cActionCount;
}
