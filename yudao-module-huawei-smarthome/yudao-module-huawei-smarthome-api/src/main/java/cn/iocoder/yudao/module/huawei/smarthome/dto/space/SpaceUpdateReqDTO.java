package cn.iocoder.yudao.module.huawei.smarthome.dto.space;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@ApiModel("华为智能家居 - 修改空间请求 DTO")
@Data
public class SpaceUpdateReqDTO {

    @ApiModelProperty(value = "空间 ID", required = true, example = "1233769875634556889")
    @NotEmpty(message = "空间 ID 不能为空")
    @Size(min = 1, max = 24, message = "空间 ID 长度必须在 1-24 之间")
    private String spaceId;

    @ApiModelProperty(value = "空间名", required = true, example = "8601会议室")
    @NotEmpty(message = "空间名不能为空")
    @Size(min = 1, max = 256, message = "空间名长度必须在 1-256 之间")
    private String spaceName;

    @ApiModelProperty(value = "描述信息", example = "更新后的客房描述")
    @Size(max = 256, message = "描述信息长度不能超过 256")
    private String desc;
}
