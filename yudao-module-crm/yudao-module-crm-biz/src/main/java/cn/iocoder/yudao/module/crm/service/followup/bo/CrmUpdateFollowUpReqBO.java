package cn.iocoder.yudao.module.crm.service.followup.bo;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 跟进信息 Update Req BO
 *
 * @author HUIHUI
 */
@Data
public class CrmUpdateFollowUpReqBO {

    @Schema(description = "数据编号", example = "3167")
    @NotNull(message = "数据编号不能为空")
    private Long bizId;

    @Schema(description = "最后跟进时间")
    @DiffLogField(name = "最后跟进时间")
    private LocalDateTime contactLastTime;

    @Schema(description = "下次联系时间")
    @DiffLogField(name = "下次联系时间")
    private LocalDateTime contactNextTime;

    @Schema(description = "最后更进内容")
    @DiffLogField(name = "最后更进内容")
    private String contactLastContent;

}
