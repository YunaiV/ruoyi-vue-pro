package cn.iocoder.dashboard.modules.system.controller.test.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

@ApiModel("字典类型 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SysTestDemoRespVO extends SysTestDemoBaseVO {

    @ApiModelProperty(value = "字典主键", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
