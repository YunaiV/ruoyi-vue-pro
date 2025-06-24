package cn.iocoder.yudao.module.huawei.smarthome.dto.scenario;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@ApiModel("华为智能家居 - 场景个数与 ECA 单元统计请求 DTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioStatisticsReqDTO {

    @ApiModelProperty(value = "空间 ID", required = true, example = "space-uuid-123")
    @NotEmpty(message = "空间 ID 不能为空")
    @Size(min = 1, max = 24, message = "空间 ID 长度必须在 1-24 之间")
    private String spaceId; // 路径参数
}
