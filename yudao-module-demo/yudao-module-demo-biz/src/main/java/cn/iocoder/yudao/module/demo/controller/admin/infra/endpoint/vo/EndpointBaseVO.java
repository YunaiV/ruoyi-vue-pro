package cn.iocoder.yudao.module.demo.controller.admin.infra.endpoint.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

/**
* 区块链节点 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class EndpointBaseVO {

    @ApiModelProperty(value = "网络编号", required = true)
    @NotNull(message = "网络编号不能为空")
    private Long netId;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "地址")
    private String url;

    @ApiModelProperty(value = "是否被墙", required = true)
    @NotNull(message = "是否被墙不能为空")
    private Boolean blocked;

    @ApiModelProperty(value = "信息")
    private String info;

}
