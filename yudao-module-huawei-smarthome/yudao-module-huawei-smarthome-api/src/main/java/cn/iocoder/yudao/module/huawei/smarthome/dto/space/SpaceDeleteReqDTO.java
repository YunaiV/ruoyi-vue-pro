package cn.iocoder.yudao.module.huawei.smarthome.dto.space;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@ApiModel("华为智能家居 - 删除空间请求 DTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceDeleteReqDTO {

    @ApiModelProperty(value = "空间 ID", required = true, example = "1233769875634556889")
    @NotEmpty(message = "空间 ID 不能为空")
    @Size(min = 1, max = 24, message = "空间 ID 长度必须在 1-24 之间")
    private String spaceId;
}
