package cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo;

import lombok.*;

import java.util.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 粉丝标签 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class WxFansTagExcelVO {

    @ExcelProperty("主键")
    private Integer id;

    @ExcelProperty("标签名称")
    private String name;

    @ExcelProperty("粉丝数量")
    private Integer count;

    @ExcelProperty("微信账号ID")
    private String wxAccountId;

    @ExcelProperty("创建时间")
    private Date createTime;

}
