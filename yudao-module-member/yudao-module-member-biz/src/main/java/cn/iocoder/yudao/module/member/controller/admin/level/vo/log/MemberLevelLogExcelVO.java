package cn.iocoder.yudao.module.member.controller.admin.level.vo.log;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会员等级记录 Excel VO
 *
 * @author owen
 */
@Data
public class MemberLevelLogExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("用户编号")
    private Long userId;

    @ExcelProperty("等级编号")
    private Long levelId;

    @ExcelProperty("会员等级")
    private Integer level;

    @ExcelProperty("享受折扣")
    private Integer discount;

    @ExcelProperty("升级经验")
    private Integer experience;

    @ExcelProperty("会员此时的经验")
    private Integer userExperience;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("描述")
    private String description;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
