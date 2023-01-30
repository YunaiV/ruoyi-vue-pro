package cn.iocoder.yudao.module.system.controller.admin.notify.vo.template;

import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 站内信模版分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotifyTemplatePageReqVO extends PageParam {

    @ApiModelProperty(value = "模版编码", example = "test_01")
    private String code;

    @ApiModelProperty(value = "模版名称", example = "我是名称")
    private String name;

    @ApiModelProperty(value = "状态", example = "1", notes = "参见 CommonStatusEnum 枚举类")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
