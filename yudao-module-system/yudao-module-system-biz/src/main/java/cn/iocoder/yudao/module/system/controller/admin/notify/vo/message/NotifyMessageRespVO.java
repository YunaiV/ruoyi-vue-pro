package cn.iocoder.yudao.module.system.controller.admin.notify.vo.message;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

@ApiModel("管理后台 - 站内信 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotifyMessageRespVO extends NotifyMessageBaseVO {

    @ApiModelProperty(value = "ID", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
