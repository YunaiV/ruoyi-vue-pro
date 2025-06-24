package cn.iocoder.yudao.module.huawei.smarthome.dto.room;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@ApiModel("华为智能家居 - 查询空间内子空间列表响应 DTO")
@Data
public class RoomListRespDTO {

    @ApiModelProperty(value = "房间集合")
    private List<RoomVO> rooms;
}
