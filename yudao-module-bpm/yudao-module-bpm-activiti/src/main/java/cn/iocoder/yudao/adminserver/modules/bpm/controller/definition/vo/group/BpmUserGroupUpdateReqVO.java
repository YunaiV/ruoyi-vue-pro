package cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.group;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("用户组更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmUserGroupUpdateReqVO extends BpmUserGroupBaseVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

}
