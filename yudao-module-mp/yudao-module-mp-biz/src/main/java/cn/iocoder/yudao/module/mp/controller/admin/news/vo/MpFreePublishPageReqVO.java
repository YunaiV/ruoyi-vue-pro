package cn.iocoder.yudao.module.mp.controller.admin.news.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 公众号已发布列表的分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpFreePublishPageReqVO extends PageParam {

    @Schema(description = "公众号账号的编号", required = true, example = "1024")
    @NotNull(message = "公众号账号的编号不能为空")
    private Long accountId;

}
