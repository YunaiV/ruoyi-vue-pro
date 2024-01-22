package cn.iocoder.yudao.module.crm.service.contact.bo;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

// TODO @puhui999：是不是搞个通用的 ReqBO 就好了
/**
 * 联系人跟进信息 Update Req BO
 *
 * @author HUIHUI
 */
@Data
public class CrmContactUpdateFollowUpReqBO {

    @Schema(description = "联系人编号", example = "3167")
    @NotNull(message = "联系人编号不能为空")
    private Long id;

    @Schema(description = "最后跟进时间")
    @DiffLogField(name = "最后跟进时间")
    @NotNull(message = "最后跟进时间不能为空")
    private LocalDateTime contactLastTime;

    @Schema(description = "下次联系时间")
    @DiffLogField(name = "下次联系时间")
    @NotNull(message = "下次联系时间不能为空")
    private LocalDateTime contactNextTime;

    @Schema(description = "最后更进内容")
    @DiffLogField(name = "最后更进内容")
    @NotNull(message = "最后更进内容不能为空")
    private String contactLastContent;

}
