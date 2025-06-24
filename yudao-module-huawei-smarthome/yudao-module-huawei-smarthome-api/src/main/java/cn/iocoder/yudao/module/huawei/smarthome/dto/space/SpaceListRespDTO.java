package cn.iocoder.yudao.module.huawei.smarthome.dto.space;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@ApiModel("华为智能家居 - 查询空间列表响应 DTO")
@Data
public class SpaceListRespDTO {

    @ApiModelProperty(value = "空间集合")
    private List<SpaceVO> spaces;
}
