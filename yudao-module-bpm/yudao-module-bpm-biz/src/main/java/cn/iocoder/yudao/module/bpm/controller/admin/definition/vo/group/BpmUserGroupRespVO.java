package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.group;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;


@Schema(title = "管理后台 - 用户组 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmUserGroupRespVO extends BpmUserGroupBaseVO {

    @Schema(title = "编号", required = true, example = "1024")
    private Long id;

    @Schema(title = "创建时间", required = true)
    private LocalDateTime createTime;

}
