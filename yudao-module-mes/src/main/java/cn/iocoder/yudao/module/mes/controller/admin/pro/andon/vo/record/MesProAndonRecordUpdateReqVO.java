package cn.iocoder.yudao.module.mes.controller.admin.pro.andon.vo.record;

import cn.iocoder.yudao.module.mes.enums.pro.MesProAndonStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 安灯呼叫记录更新 Request VO")
@Data
public class MesProAndonRecordUpdateReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "处置时间")
    private LocalDateTime handleTime;

    @Schema(description = "处置人编号", example = "100")
    private Long handlerUserId;

    @Schema(description = "备注", example = "已修复")
    private String remark;

    @Schema(description = "处置状态", example = "1")
    @NotNull(message = "处置状态不能为空")
    private Integer status;

    @AssertTrue(message = "标记已处置时，处置时间不能为空")
    @JsonIgnore
    public boolean isHandleTimeValid() {
        if (!MesProAndonStatusEnum.HANDLED.getStatus().equals(status)) {
            return true;
        }
        return handleTime != null;
    }

    @AssertTrue(message = "标记已处置时，处置人不能为空")
    @JsonIgnore
    public boolean isHandlerUserIdValid() {
        if (!MesProAndonStatusEnum.HANDLED.getStatus().equals(status)) {
            return true;
        }
        return handlerUserId != null;
    }

}
