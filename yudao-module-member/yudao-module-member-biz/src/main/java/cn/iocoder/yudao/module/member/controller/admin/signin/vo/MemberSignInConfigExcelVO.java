package cn.iocoder.yudao.module.member.controller.admin.signin.vo;

import lombok.*;

import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 积分签到规则 Excel VO
 *
 * @author QingX
 */
@Data
public class MemberSignInConfigExcelVO {

    @ExcelProperty("签到第x天")
    private Integer day;

    @ExcelProperty("签到天数对应分数")
    private Integer point;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
