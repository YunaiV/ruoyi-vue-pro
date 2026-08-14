package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 招聘渠道 Response VO")
@Data
public class HrmRecruitChannelRespVO {

    @Schema(description = "招聘渠道编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "是否系统内置", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean systemFlag;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "渠道名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "BOSS 直聘")
    private String name;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer sort;

    @Schema(description = "备注", example = "常用渠道")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
