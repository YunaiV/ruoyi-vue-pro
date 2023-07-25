package cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.*;

@Schema(description = "管理后台 - 用户积分记录更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemberPointRecordUpdateReqVO extends MemberPointRecordBaseVO {

    @Schema(description = "自增主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "31457")
    @NotNull(message = "自增主键不能为空")
    private Long id;

}
