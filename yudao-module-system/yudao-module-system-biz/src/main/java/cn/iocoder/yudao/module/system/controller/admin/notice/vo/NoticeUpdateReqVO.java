package cn.iocoder.yudao.module.system.controller.admin.notice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 岗位公告更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeUpdateReqVO extends NoticeBaseVO {

    @Schema(description = "岗位公告编号", required = true, example = "1024")
    @NotNull(message = "岗位公告编号不能为空")
    private Long id;

}
