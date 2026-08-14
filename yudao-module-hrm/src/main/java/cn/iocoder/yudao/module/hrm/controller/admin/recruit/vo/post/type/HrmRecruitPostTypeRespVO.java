package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.post.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 招聘职位类型 Response VO")
@Data
public class HrmRecruitPostTypeRespVO {

    @Schema(description = "职位类型编号", example = "1024")
    private Long id;

    @Schema(description = "类型名称", example = "后端开发")
    private String name;

    @Schema(description = "父类型编号", example = "0")
    private Long parentId;

    @Schema(description = "排序", example = "10")
    private Integer sort;

    @Schema(description = "状态，参见 common_status", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
