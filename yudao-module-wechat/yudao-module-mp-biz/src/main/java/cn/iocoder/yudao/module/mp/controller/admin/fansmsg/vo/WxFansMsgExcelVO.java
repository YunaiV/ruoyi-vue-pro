package cn.iocoder.yudao.module.mp.controller.admin.fansmsg.vo;

import lombok.*;

import java.util.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 粉丝消息表  Excel VO
 *
 * @author 芋道源码
 */
@Data
public class WxFansMsgExcelVO {

    @ExcelProperty("主键")
    private Integer id;

    @ExcelProperty("用户标识")
    private String openid;

    @ExcelProperty("昵称")
    private byte[] nickname;

    @ExcelProperty("头像地址")
    private String headimgUrl;

    @ExcelProperty("微信账号ID")
    private String wxAccountId;

    @ExcelProperty("消息类型")
    private String msgType;

    @ExcelProperty("内容")
    private String content;

    @ExcelProperty("最近一条回复内容")
    private String resContent;

    @ExcelProperty("是否已回复")
    private String isRes;

    @ExcelProperty("微信素材ID")
    private String mediaId;

    @ExcelProperty("微信图片URL")
    private String picUrl;

    @ExcelProperty("本地图片路径")
    private String picPath;

    @ExcelProperty("创建时间")
    private Date createTime;

}
