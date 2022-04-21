package cn.iocoder.yudao.module.system.controller.admin.notice.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 通知公告 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class NoticeBaseVO {

    @ApiModelProperty(value = "公告标题", required = true, example = "小博主")
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 50, message = "公告标题不能超过50个字符")
    private String title;

    @ApiModelProperty(value = "公告类型", required = true, example = "小博主")
    @NotNull(message = "公告类型不能为空")
    private Integer type;

    @ApiModelProperty(value = "公告内容", required = true, example = "半生编码")
    private String content;

    @ApiModelProperty(value = "状态", required = true, example = "1", notes = "参见 CommonStatusEnum 枚举类")
    private Integer status;

}
