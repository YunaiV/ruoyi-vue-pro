package cn.iocoder.yudao.module.crm.controller.admin.clue.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;


/**
 * 线索 Excel VO
 *
 * @author Wanwan
 */
@Data
public class CrmClueExcelVO {

    @ExcelProperty("编号，主键自增")
    private Long id;

    @ExcelProperty(value = "转化状态", converter = DictConvert.class)
    @DictFormat("infra_boolean_string") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Boolean transformStatus;

    @ExcelProperty(value = "跟进状态", converter = DictConvert.class)
    @DictFormat("infra_boolean_string") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Boolean followUpStatus;

    @ExcelProperty("线索名称")
    private String name;

    @ExcelProperty("客户id")
    private Long customerId;

    @ExcelProperty("下次联系时间")
    private LocalDateTime contactNextTime;

    @ExcelProperty("电话")
    private String telephone;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("地址")
    private String address;

    @ExcelProperty("负责人的用户编号")
    private Long ownerUserId;

    @ExcelProperty("最后跟进时间")
    private LocalDateTime contactLastTime;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
