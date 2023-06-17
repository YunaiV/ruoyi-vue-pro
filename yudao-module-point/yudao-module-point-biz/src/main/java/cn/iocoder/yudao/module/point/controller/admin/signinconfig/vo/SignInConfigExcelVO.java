package cn.iocoder.yudao.module.point.controller.admin.signinconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 积分签到规则 Excel VO
 *
 * @author QingX
 */
@Data
public class SignInConfigExcelVO {

    @ExcelProperty("签到第x天")
    private Integer day;

    @ExcelProperty("签到天数对应分数")
    private Integer point;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
