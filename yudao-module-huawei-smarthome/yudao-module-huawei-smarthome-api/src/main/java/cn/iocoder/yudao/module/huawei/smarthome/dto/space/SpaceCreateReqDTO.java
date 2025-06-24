package cn.iocoder.yudao.module.huawei.smarthome.dto.space;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("华为智能家居 - 创建空间请求 DTO")
@Data
public class SpaceCreateReqDTO {

    @ApiModelProperty(value = "空间名", required = true, example = "客厅一号")
    @NotEmpty(message = "空间名不能为空")
    @Size(min = 1, max = 256, message = "空间名长度必须在 1-256 之间")
    private String spaceName;

    @ApiModelProperty(value = "描述信息", example = "这是位于一楼的客厅")
    @Size(max = 256, message = "描述信息长度不能超过 256")
    private String desc;

}
