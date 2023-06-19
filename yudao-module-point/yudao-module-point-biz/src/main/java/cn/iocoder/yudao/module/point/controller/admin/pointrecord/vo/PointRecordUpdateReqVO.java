package cn.iocoder.yudao.module.point.controller.admin.pointrecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 用户积分记录更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PointRecordUpdateReqVO extends PointRecordBaseVO {

    @Schema(description = "自增主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "31457")
    @NotNull(message = "自增主键不能为空")
    private Long id;

}
