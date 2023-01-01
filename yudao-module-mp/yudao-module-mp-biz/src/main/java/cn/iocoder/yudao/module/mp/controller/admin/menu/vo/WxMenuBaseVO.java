package cn.iocoder.yudao.module.mp.controller.admin.menu.vo;

import lombok.*;
import io.swagger.annotations.*;

/**
 * 微信菜单 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class WxMenuBaseVO {

    @ApiModelProperty(value = "父ID")
    private String parentId;

    @ApiModelProperty(value = "菜单名称")
    private String menuName;

    @ApiModelProperty(value = "菜单类型 1文本消息；2图文消息；3网址链接；4小程序")
    private String menuType;

    @ApiModelProperty(value = "菜单等级")
    private String menuLevel;

    @ApiModelProperty(value = "模板ID")
    private String tplId;

    @ApiModelProperty(value = "菜单URL")
    private String menuUrl;

    @ApiModelProperty(value = "排序")
    private String menuSort;

    @ApiModelProperty(value = "微信账号ID")
    private String wxAccountId;

    @ApiModelProperty(value = "小程序appid")
    private String miniprogramAppid;

    @ApiModelProperty(value = "小程序页面路径")
    private String miniprogramPagepath;

}
