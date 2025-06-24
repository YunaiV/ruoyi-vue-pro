package cn.iocoder.yudao.module.huawei.smarthome.dto.scenario;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@ApiModel("华为智能家居 - 场景个数与 ECA 单元统计响应 DTO")
@Data
public class ScenarioStatisticsRespDTO {

    @ApiModelProperty(value = "空间 ID", required = true, example = "space-uuid-123")
    private String spaceId;

    @ApiModelProperty(value = "场景总个数", required = true, example = "23")
    private String scenarioCount; // 文档中为 String 类型

    @ApiModelProperty(value = "统计集合", required = true)
    private List<ScenarioStatisticDetailVO> statistics;
}
