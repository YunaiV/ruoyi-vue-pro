package cn.iocoder.yudao.module.demo.controller.admin.infra.net.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

/**
* 区块链网络 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class NetBaseVO {

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "区块浏览器")
    private String explorer;

    @ApiModelProperty(value = "原生代币")
    private String symbol;

    @ApiModelProperty(value = "节点")
    private String endpoint;

    @ApiModelProperty(value = "网络类型")
    private String type;

    @ApiModelProperty(value = "信息")
    private String info;

}
