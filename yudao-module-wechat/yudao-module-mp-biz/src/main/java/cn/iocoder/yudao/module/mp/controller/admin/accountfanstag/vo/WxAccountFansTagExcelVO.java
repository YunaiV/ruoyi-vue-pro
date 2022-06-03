package cn.iocoder.yudao.module.mp.controller.admin.accountfanstag.vo;

import lombok.*;

import java.util.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 粉丝标签关联 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class WxAccountFansTagExcelVO {

    @ExcelProperty("主键")
    private Integer id;

    @ExcelProperty("用户标识")
    private String openid;

    @ExcelProperty("标签ID")
    private String tagId;

    @ExcelProperty("微信账号ID")
    private String wxAccountId;

    @ExcelProperty("创建时间")
    private Date createTime;

}
