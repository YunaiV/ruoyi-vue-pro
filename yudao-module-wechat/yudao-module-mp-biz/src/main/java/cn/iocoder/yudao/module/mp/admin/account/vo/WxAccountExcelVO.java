package cn.iocoder.yudao.module.mp.admin.account.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 公众号账户 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class WxAccountExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("公众号名称")
    private String name;

    @ExcelProperty("公众号账户")
    private String account;

    @ExcelProperty("公众号appid")
    private String appid;

    @ExcelProperty("公众号密钥")
    private String appsecret;

    @ExcelProperty("公众号url")
    private String url;

    @ExcelProperty("公众号token")
    private String token;

    @ExcelProperty("加密密钥")
    private String aeskey;

    @ExcelProperty("二维码图片URL")
    private String qrUrl;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private Date createTime;

}
