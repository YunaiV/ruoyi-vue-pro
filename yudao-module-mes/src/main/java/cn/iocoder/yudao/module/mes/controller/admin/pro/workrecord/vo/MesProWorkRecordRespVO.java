package cn.iocoder.yudao.module.mes.controller.admin.pro.workrecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 当前用户工作站绑定状态 Response VO")
@Data
@Accessors(chain = true)
public class MesProWorkRecordRespVO {

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "用户昵称", example = "张三")
    private String userNickname;

    @Schema(description = "工作站编号", example = "1")
    private Long workstationId;

    @Schema(description = "工作站编码", example = "WS-001")
    private String workstationCode;

    @Schema(description = "工作站名称", example = "注塑工作站")
    private String workstationName;

    @Schema(description = "当前状态", example = "1")
    private Integer type;

    @Schema(description = "上工时间")
    private LocalDateTime clockInTime;

    @Schema(description = "下工时间")
    private LocalDateTime clockOutTime;

}
