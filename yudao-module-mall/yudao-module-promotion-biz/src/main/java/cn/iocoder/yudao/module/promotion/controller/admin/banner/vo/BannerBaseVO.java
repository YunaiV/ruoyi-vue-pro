package cn.iocoder.yudao.module.promotion.controller.admin.banner.vo;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Banner Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 * @author xia
 */
@Data
public class BannerBaseVO {

    @ApiModelProperty(value = "标题", required = true)
    @NotNull(message = "标题不能为空")
    private String title;

    @ApiModelProperty(value = "跳转链接", required = true)
    @NotNull(message = "跳转链接不能为空")
    private String url;

    @ApiModelProperty(value = "图片地址", required = true)
    @NotNull(message = "图片地址不能为空")
    private String picUrl;

    @ApiModelProperty(value = "排序", required = true)
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @ApiModelProperty(value = "状态", required = true)
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String memo;

}
