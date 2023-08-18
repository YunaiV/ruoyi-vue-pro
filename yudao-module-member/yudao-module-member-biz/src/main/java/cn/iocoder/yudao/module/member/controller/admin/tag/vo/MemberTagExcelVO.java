package cn.iocoder.yudao.module.member.controller.admin.tag.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会员标签 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class MemberTagExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("标签名称")
    private String name;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
