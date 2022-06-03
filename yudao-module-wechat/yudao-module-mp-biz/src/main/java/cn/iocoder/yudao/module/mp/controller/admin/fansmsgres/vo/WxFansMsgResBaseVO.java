package cn.iocoder.yudao.module.mp.controller.admin.fansmsgres.vo;

import lombok.*;
import io.swagger.annotations.*;

/**
 * 回复粉丝消息历史表  Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class WxFansMsgResBaseVO {

    @ApiModelProperty(value = "粉丝消息ID")
    private String fansMsgId;

    @ApiModelProperty(value = "回复内容")
    private String resContent;

}
