package cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

@ApiModel("动态表单 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmFormRespVO extends BpmFormBaseVO {

    @ApiModelProperty(value = "表单编号", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
