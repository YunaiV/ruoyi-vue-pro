package cn.iocoder.yudao.module.system.controller.admin.user.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel(value = "管理后台 - 用户导出 Request VO", description = "参数和 UserPageReqVO 是一致的")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserExportReqVO {

    @ApiModelProperty(value = "用户账号", example = "yudao", notes = "模糊匹配")
    private String username;

    @ApiModelProperty(value = "手机号码", example = "yudao", notes = "模糊匹配")
    private String mobile;

    @ApiModelProperty(value = "展示状态", example = "1", notes = "参见 CommonStatusEnum 枚举类")
    private Integer status;

    @ApiModelProperty(value = "创建时间", example = "[2022-07-01 00:00:00,2022-07-01 23:59:59]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @ApiModelProperty(value = "部门编号", example = "1024", notes = "同时筛选子部门")
    private Long deptId;

}
