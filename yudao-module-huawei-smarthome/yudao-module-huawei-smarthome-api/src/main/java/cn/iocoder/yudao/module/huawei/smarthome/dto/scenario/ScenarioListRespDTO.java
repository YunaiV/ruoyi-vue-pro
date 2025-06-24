package cn.iocoder.yudao.module.huawei.smarthome.dto.scenario;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@ApiModel("华为智能家居 - 场景列表响应 DTO")
@Data
public class ScenarioListRespDTO {

    @ApiModelProperty(value = "场景集合", required = true)
    private List<ScenarioVO> scenarios;
}
