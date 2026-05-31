package cn.iocoder.yudao.module.im.controller.admin.friend.vo.request;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.im.enums.friend.ImFriendAddSourceEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * IM 好友申请 - 发起 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IM 好友申请发起 Request VO")
@Data
public class ImFriendRequestApplyReqVO {

    @Schema(description = "接收方用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "接收方用户编号不能为空")
    private Long toUserId;

    @Schema(description = "申请理由", example = "我是芋艿（一种食材）")
    @Size(max = 255, message = "申请理由最多 255 个字符")
    private String applyContent;

    @Schema(description = "对接收方的备注（仅自己可见）", example = "老张")
    @Size(max = 16, message = "好友备注最多 16 个字符")
    private String displayName;

    @Schema(description = "添加来源", example = "1")
    @InEnum(ImFriendAddSourceEnum.class)
    private Integer addSource;

}
