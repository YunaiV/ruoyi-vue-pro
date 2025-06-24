package cn.iocoder.yudao.module.huawei.smarthome.dto.scenario;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@ApiModel("华为智能家居 - 查询场景实例详情响应 DTO")
@Data
public class ScenarioDetailRespDTO {

    @ApiModelProperty(value = "场景详情 (ECA 数据)", required = true,
            notes = "格式相对比较复杂，接口暂不提供具体描述，直接透传Map<String, Object>")
    private Map<String, Object> detail; // ECA 数据，结构复杂，用Map接收
}
