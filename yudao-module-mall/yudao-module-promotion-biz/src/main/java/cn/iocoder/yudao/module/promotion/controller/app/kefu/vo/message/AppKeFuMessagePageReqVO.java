package cn.iocoder.yudao.module.promotion.controller.app.kefu.vo.message;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "用户 App - 客服消息分页 Request VO")
@Data
@ToString(callSuper = true)
public class AppKeFuMessagePageReqVO extends PageParam {

    @Schema(description = "会话编号", example = "12580")
    private Long conversationId;

}