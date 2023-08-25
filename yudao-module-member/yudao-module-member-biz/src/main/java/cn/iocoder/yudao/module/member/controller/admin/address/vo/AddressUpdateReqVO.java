package cn.iocoder.yudao.module.member.controller.admin.address.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 用户收件地址更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AddressUpdateReqVO extends AddressBaseVO {

    @Schema(description = "收件地址编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "7380")
    @NotNull(message = "收件地址编号不能为空")
    private Long id;

}
