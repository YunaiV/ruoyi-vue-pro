package cn.iocoder.yudao.module.huawei.smarthome.dto.room;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@ApiModel("华为智能家居 - 查询空间内子空间信息请求 DTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomListBySpaceReqDTO {

    @ApiModelProperty(value = "空间 ID", required = true, example = "877422008017620608")
    @NotEmpty(message = "空间 ID 不能为空")
    @Size(min = 1, max = 24, message = "空间 ID 长度必须在 1-24 之间")
    private String spaceId;
}
