package cn.iocoder.yudao.module.crm.controller.admin.callcenter.vo;

import cn.iocoder.yudao.module.crm.framework.operatelog.core.CrmCustomerParseFunction;
import cn.iocoder.yudao.module.crm.framework.operatelog.core.SysAdminUserParseFunction;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - CRM 商机创建/更新 Request VO")
@Data
public class CrmCallcenterCallReqVO {

    @Schema(description = "厂商ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @DiffLogField(name = "厂商ID")
    @NotNull(message = "厂商ID不能为空")
    private Long manufacturerId;

    @Schema(description = "外呼线索或客户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    @DiffLogField(name = "外呼线索或客户id")
    @NotNull(message = "外呼线索或客户id不能为空")
    private String callId;


    @Schema(description = "外呼线索或客户类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1=线索，2=客户")
    @DiffLogField(name = "外呼线索或客户类型")
    @NotNull(message = "外呼线索或客户类型不能为空")
    private String callType;

}
