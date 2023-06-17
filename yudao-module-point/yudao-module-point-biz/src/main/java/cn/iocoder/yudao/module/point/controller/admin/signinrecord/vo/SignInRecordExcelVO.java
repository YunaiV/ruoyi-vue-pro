package cn.iocoder.yudao.module.point.controller.admin.signinrecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 用户签到积分 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class SignInRecordExcelVO {

    @ExcelProperty("签到自增id")
    private Long id;

    @ExcelProperty("签到用户")
    private Integer userId;

    @ExcelProperty("第几天签到")
    private Integer day;

    @ExcelProperty("签到的分数")
    private Integer point;

    @ExcelProperty("签到时间")
    private LocalDateTime createTime;

}
