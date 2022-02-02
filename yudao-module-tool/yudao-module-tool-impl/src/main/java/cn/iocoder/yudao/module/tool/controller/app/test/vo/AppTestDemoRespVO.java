package cn.iocoder.yudao.module.tool.controller.app.test.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

@ApiModel("用户 APP - 字典类型 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppTestDemoRespVO extends AppTestDemoBaseVO {

    @ApiModelProperty(value = "编号", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
