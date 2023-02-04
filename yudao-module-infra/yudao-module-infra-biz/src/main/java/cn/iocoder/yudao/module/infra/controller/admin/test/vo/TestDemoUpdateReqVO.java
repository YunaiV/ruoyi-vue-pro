package cn.iocoder.yudao.module.infra.controller.admin.test.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 字典类型更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestDemoUpdateReqVO extends TestDemoBaseVO {

    @Schema(description = "编号", required = true)
    @NotNull(message = "编号不能为空")
    private Long id;

}
