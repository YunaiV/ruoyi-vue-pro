package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - HRM 员工通知发送结果 Response VO")
@Data
@Accessors(chain = true)
public class HrmEmployeeNotifyRespVO {

    @Schema(description = "发送成功数量", example = "10")
    private Integer successCount;

    @Schema(description = "无后台账号跳过数量", example = "2")
    private Integer skippedCount;

    @Schema(description = "发送失败数量", example = "1")
    private Integer failureCount;

}
