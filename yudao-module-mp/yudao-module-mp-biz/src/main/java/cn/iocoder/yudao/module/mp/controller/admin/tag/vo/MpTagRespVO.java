package cn.iocoder.yudao.module.mp.controller.admin.tag.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@Schema(description = "管理后台 - 公众号标签 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpTagRespVO extends MpTagBaseVO {

    @Schema(description = "编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "此标签下粉丝数量", required = true, example = "0")
    private Integer count;

    @Schema(description = "创建时间", required = true)
    private Date createTime;

}
