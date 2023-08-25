package cn.iocoder.yudao.module.member.controller.admin.address.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 用户收件地址 Excel VO
 *
 * @author 绮梦
 */
@Data
public class AddressExcelVO {

    @ExcelProperty("收件地址编号")
    private Long id;

    @ExcelProperty("收件人名称")
    private String name;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("地区编码")
    private Long areaId;

    @ExcelProperty("收件详细地址")
    private String detailAddress;

    @ExcelProperty("是否默认")
    private Boolean defaultStatus;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
