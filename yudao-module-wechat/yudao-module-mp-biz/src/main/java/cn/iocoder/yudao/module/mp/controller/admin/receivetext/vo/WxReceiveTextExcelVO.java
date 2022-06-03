package cn.iocoder.yudao.module.mp.controller.admin.receivetext.vo;

import lombok.*;

import java.util.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 回复关键字 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class WxReceiveTextExcelVO {

    @ExcelProperty("主键")
    private Integer id;

    @ExcelProperty("关键字")
    private String receiveText;

    @ExcelProperty("消息类型 1文本消息；2图文消息；")
    private String msgType;

    @ExcelProperty("模板ID")
    private String tplId;

    @ExcelProperty("微信账号ID")
    private String wxAccountId;

    @ExcelProperty("创建时间")
    private Date createTime;

}
