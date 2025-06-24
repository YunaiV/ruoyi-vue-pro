package cn.iocoder.yudao.module.huawei.smarthome.dto.space;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("华为智能家居 - 空间信息 VO")
@Data
public class SpaceVO {

    @ApiModelProperty(value = "空间 ID", required = true, example = "1231236576786445234234")
    private String spaceId;

    @ApiModelProperty(value = "空间名", required = true, example = "8601")
    private String spaceName;

    @ApiModelProperty(value = "描述信息", example = "客房")
    private String desc;

    // TODO: 未来如果接口返回更多信息，可以在这里添加
    // 例如：创建时间、子空间列表等，取决于实际API响应和业务需求
}
