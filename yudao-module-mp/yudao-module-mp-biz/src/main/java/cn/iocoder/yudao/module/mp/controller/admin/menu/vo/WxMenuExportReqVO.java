package cn.iocoder.yudao.module.mp.controller.admin.menu.vo;

import lombok.*;

import java.util.*;

import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel(value = "管理后台 - 微信菜单 Excel 导出 Request VO", description = "参数和 WxMenuPageReqVO 是一致的")
@Data
public class WxMenuExportReqVO {

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

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始创建时间")
    private Date beginCreateTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束创建时间")
    private Date endCreateTime;

}
