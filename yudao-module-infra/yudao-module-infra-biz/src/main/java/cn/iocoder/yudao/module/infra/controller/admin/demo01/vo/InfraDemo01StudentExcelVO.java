package cn.iocoder.yudao.module.infra.controller.admin.demo01.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * 学生 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class InfraDemo01StudentExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("名字")
    private String name;

    @ExcelProperty("简介")
    private String description;

    @ExcelProperty("出生日期")
    private LocalDateTime birthday;

    @ExcelProperty(value = "性别", converter = DictConvert.class)
    @DictFormat("system_user_sex") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Integer sex;

    @ExcelProperty(value = "是否有效", converter = DictConvert.class)
    @DictFormat("infra_boolean_string") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Boolean enabled;

    @ExcelProperty(value = "支付方式", converter = DictConvert.class)
    @DictFormat("pay_channel_code") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private String payChannels;

    @ExcelProperty("头像")
    private String avatar;

    @ExcelProperty("附件")
    private String video;

    @ExcelProperty("备注")
    private String memo;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}