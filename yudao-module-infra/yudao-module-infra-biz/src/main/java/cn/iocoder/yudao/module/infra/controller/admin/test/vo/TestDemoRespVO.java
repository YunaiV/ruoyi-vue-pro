package cn.iocoder.yudao.module.infra.controller.admin.test.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(title = "管理后台 - 字典类型 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestDemoRespVO extends TestDemoBaseVO {

    @Schema(title = "编号", required = true)
    private Long id;

    @Schema(title = "创建时间", required = true)
    private LocalDateTime createTime;

}
