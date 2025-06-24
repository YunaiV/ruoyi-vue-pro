package cn.iocoder.yudao.module.huawei.smarthome.dto.room;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@ApiModel("华为智能家居 - 修改子空间名称请求 DTO")
@Data
public class RoomUpdateReqDTO {

    @ApiModelProperty(value = "空间 ID", required = true, example = "877422003647155840")
    @NotEmpty(message = "空间 ID 不能为空")
    @Size(min = 1, max = 24, message = "空间 ID 长度必须在 1-24 之间")
    private String spaceId; // 这个是路径参数，但在请求体中也可能需要，根据实际API行为

    @ApiModelProperty(value = "区域 ID (子空间/房间 ID)", required = true, example = "1651216042886")
    @NotEmpty(message = "区域 ID 不能为空")
    @Size(min = 1, max = 24, message = "区域 ID 长度必须在 1-24 之间")
    private String roomId; // 华为文档中，这个参数不在请求体，而是在内部使用或通过其他方式关联。但为了明确，先定义。实际调用时可能只用 roomName 和 desc。

    @ApiModelProperty(value = "区域名 (子空间/房间名)", required = true, example = "阳台")
    @NotEmpty(message = "区域名不能为空")
    @Size(min = 1, max = 256, message = "区域名长度必须在 1-256 之间")
    private String roomName;

    @ApiModelProperty(value = "描述信息", example = "洗衣房阳台")
    @Size(max = 256, message = "描述信息长度不能超过 256")
    private String desc;
}
