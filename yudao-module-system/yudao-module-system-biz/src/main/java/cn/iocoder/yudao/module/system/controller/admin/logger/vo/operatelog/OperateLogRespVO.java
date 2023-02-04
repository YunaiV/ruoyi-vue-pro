package cn.iocoder.yudao.module.system.controller.admin.logger.vo.operatelog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 操作日志 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OperateLogRespVO extends OperateLogBaseVO {

    @Schema(description = "日志编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "用户昵称", required = true, example = "芋艿")
    private String userNickname;

}
