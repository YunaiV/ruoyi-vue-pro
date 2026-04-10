package cn.iocoder.yudao.module.mes.controller.admin.pro.andon.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 安灯呼叫配置 Response VO")
@Data
public class MesProAndonConfigRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "呼叫原因", example = "设备故障")
    private String reason;

    @Schema(description = "级别", example = "1")
    private Integer level;

    @Schema(description = "处置人角色编号", example = "10")
    private Long handlerRoleId;

    @Schema(description = "处置人编号", example = "100")
    private Long handlerUserId;

    @Schema(description = "处置人昵称", example = "张三")
    private String handlerUserNickname;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
