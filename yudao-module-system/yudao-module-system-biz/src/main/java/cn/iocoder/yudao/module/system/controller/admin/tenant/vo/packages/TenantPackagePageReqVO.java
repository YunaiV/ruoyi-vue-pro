package cn.iocoder.yudao.module.system.controller.admin.tenant.vo.packages;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 租户套餐分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TenantPackagePageReqVO extends PageParam {

    @ApiModelProperty(value = "套餐名", example = "VIP")
    private String name;

    @ApiModelProperty(value = "状态", example = "1")
    private Integer status;

    @ApiModelProperty(value = "备注", example = "好")
    private String remark;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime[] createTime;
}
