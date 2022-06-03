package cn.iocoder.yudao.module.mp.controller.admin.menu.vo;

import lombok.*;

import java.util.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 微信菜单 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class WxMenuExcelVO {

    @ExcelProperty("主键")
    private Integer id;

    @ExcelProperty("父ID")
    private String parentId;

    @ExcelProperty("菜单名称")
    private String menuName;

    @ExcelProperty("菜单类型 1文本消息；2图文消息；3网址链接；4小程序")
    private String menuType;

    @ExcelProperty("菜单等级")
    private String menuLevel;

    @ExcelProperty("模板ID")
    private String tplId;

    @ExcelProperty("菜单URL")
    private String menuUrl;

    @ExcelProperty("排序")
    private String menuSort;

    @ExcelProperty("微信账号ID")
    private String wxAccountId;

    @ExcelProperty("小程序appid")
    private String miniprogramAppid;

    @ExcelProperty("小程序页面路径")
    private String miniprogramPagepath;

    @ExcelProperty("创建时间")
    private Date createTime;

}
