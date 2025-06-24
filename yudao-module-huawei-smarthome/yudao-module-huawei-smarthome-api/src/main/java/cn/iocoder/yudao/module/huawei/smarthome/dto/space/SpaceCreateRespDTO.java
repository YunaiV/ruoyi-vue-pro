package cn.iocoder.yudao.module.huawei.smarthome.dto.space;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("华为智能家居 - 创建空间响应 DTO")
@Data
public class SpaceCreateRespDTO {

    @ApiModelProperty(value = "唯一的空间 ID", required = true, example = "1233478845322")
    private String spaceId;
}
