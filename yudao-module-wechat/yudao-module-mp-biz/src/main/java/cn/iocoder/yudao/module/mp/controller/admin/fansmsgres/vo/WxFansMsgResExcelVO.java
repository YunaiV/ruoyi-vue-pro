package cn.iocoder.yudao.module.mp.controller.admin.fansmsgres.vo;

import lombok.*;

import java.util.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 回复粉丝消息历史表  Excel VO
 *
 * @author 芋道源码
 */
@Data
public class WxFansMsgResExcelVO {

    @ExcelProperty("主键")
    private Integer id;

    @ExcelProperty("粉丝消息ID")
    private String fansMsgId;

    @ExcelProperty("回复内容")
    private String resContent;

    @ExcelProperty("创建时间")
    private Date createTime;

}
