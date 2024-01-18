package cn.iocoder.yudao.module.crm.service.customer.bo;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

// TODO @puhui999：是不是搞个通用的 ReqBO 就好了
/**
 * 跟进信息 Update Req BO
 *
 * @author HUIHUI
 */
@Data
public class CrmCustomerUpdateFollowUpReqBO {

    @Schema(description = "主键", example = "3167")
    private Long id;

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
