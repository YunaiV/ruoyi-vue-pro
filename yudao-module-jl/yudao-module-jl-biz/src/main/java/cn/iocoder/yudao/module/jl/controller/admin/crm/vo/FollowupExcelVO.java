package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;


/**
 * 销售线索跟进，可以是跟进客户，也可以是跟进线索 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class FollowupExcelVO {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("内容")
    private String content;

    @ExcelProperty("客户id")
    private Long customerId;

    @ExcelProperty("跟进实体的 id，项目、线索、款项，客户等")
    private Long refId;

    @ExcelProperty(value = "跟进类型：日常联系、销售线索、催款等", converter = DictConvert.class)
    @DictFormat("followup_type") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Integer type;

}
