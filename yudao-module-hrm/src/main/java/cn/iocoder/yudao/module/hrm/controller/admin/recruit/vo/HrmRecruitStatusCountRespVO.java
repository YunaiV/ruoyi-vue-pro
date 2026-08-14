package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - HRM 招聘状态数量 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HrmRecruitStatusCountRespVO {

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "数量", example = "12")
    private Long count;

}
