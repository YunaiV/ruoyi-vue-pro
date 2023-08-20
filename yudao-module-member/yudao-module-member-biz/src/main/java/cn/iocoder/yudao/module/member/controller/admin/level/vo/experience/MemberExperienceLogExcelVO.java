package cn.iocoder.yudao.module.member.controller.admin.level.vo.experience;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.member.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * 会员经验记录 Excel VO
 *
 * @author owen
 */
@Data
public class MemberExperienceLogExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("用户编号")
    private Long userId;

    @ExcelProperty(value = "业务类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MEMBER_EXPERIENCE_BIZ_TYPE)
    private Integer bizType;

    @ExcelProperty("业务编号")
    private String bizId;

    @ExcelProperty("标题")
    private String title;

    @ExcelProperty("经验")
    private Integer experience;

    @ExcelProperty("变更后的经验")
    private Integer totalExperience;

    @ExcelProperty("描述")
    private String description;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
