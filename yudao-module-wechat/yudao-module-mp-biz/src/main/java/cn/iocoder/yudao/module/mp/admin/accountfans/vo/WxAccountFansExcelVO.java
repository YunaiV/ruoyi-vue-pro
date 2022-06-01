package cn.iocoder.yudao.module.mp.admin.accountfans.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 微信公众号粉丝 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class WxAccountFansExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("用户标识")
    private String openid;

    @ExcelProperty("订阅状态，0未关注，1已关注")
    private String subscribeStatus;

    @ExcelProperty("订阅时间")
    private Date subscribeTime;

    @ExcelProperty("昵称")
    private byte[] nickname;

    @ExcelProperty("性别，1男，2女，0未知")
    private String gender;

    @ExcelProperty("语言")
    private String language;

    @ExcelProperty("国家")
    private String country;

    @ExcelProperty("省份")
    private String province;

    @ExcelProperty("城市")
    private String city;

    @ExcelProperty("头像地址")
    private String headimgUrl;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("微信公众号ID")
    private String wxAccountId;

    @ExcelProperty("微信公众号appid")
    private String wxAccountAppid;

    @ExcelProperty("创建时间")
    private Date createTime;

}
