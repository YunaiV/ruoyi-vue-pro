package cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel(value = "管理后台 - 租户 Excel 导出 Request VO", description = "参数和 TenantPageReqVO 是一致的")
@Data
public class TenantExportReqVO {

    @ApiModelProperty(value = "租户名", example = "芋道")
    private String name;

    @ApiModelProperty(value = "联系人", example = "芋艿")
    private String contactName;

    @ApiModelProperty(value = "联系手机", example = "15601691300")
    private String contactMobile;

    @ApiModelProperty(value = "租户状态（0正常 1停用）", example = "1")
    private Integer status;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "创建时间")
    private Date[] createTime;

}
