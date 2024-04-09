package cn.iocoder.yudao.module.crm.controller.admin.callcenter.vo;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - CRM 呼收中心 Request VO")
@Data
public class CrmCallcenterCallReqVO {

    @Schema(description = "厂商ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @DiffLogField(name = "厂商ID")
    @NotNull(message = "厂商ID不能为空")
    private Long manufacturerId;

    @Schema(description = "外呼线索或客户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    @DiffLogField(name = "外呼线索或客户id")
    @NotNull(message = "外呼线索或客户id不能为空")
    private Long callId;


    @Schema(description = "外呼线索或客户类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1=线索，2=客户")
    @DiffLogField(name = "外呼线索或客户类型")
    @NotNull(message = "外呼线索或客户类型不能为空")
    private Integer callType;

}
