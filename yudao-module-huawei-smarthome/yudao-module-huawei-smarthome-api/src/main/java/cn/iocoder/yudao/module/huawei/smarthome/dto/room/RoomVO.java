package cn.iocoder.yudao.module.huawei.smarthome.dto.room;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("华为智能家居 - 子空间/区域信息 VO")
@Data
public class RoomVO {

    @ApiModelProperty(value = "房间 ID（非全局唯一，空间内唯一）", required = true, example = "1651216042886")
    private String roomId;

    @ApiModelProperty(value = "房间名", required = true, example = "客厅")
    private String roomName;

    @ApiModelProperty(value = "描述信息", example = "这是主客厅")
    private String desc;
}
