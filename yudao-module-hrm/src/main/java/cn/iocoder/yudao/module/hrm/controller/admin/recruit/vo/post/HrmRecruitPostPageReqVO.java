package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.post;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - HRM 招聘职位分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmRecruitPostPageReqVO extends PageParam {

    @Schema(description = "职位名称，模糊匹配", example = "Java")
    private String postName;

    @Schema(description = "工作性质", example = "1")
    private Integer jobNature;

    @Schema(description = "工作城市地区编号", example = "310115")
    private Integer areaId;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "招聘负责人员工编号", example = "200")
    private Long ownerEmployeeId;

    @Schema(description = "职位类型编号", example = "100")
    private Long postTypeId;

    @Schema(description = "职位状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
