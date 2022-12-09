package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.group;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

@Schema(title = "管理后台 - 用户组更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmUserGroupUpdateReqVO extends BpmUserGroupBaseVO {

    @Schema(title = "编号", required = true, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

}
