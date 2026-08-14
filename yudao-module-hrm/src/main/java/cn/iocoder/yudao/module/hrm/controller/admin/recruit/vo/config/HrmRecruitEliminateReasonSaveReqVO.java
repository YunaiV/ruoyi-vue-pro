package cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.config;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 招聘淘汰原因保存 Request VO")
@Data
public class HrmRecruitEliminateReasonSaveReqVO {

    @Schema(description = "淘汰原因列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "淘汰原因不能为空")
    @DiffLogField(name = "淘汰原因")
    private List<@NotBlank(message = "淘汰原因不能为空") String> reasons;

}
