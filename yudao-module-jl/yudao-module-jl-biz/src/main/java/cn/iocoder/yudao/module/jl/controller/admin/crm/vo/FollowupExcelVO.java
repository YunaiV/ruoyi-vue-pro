package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 销售跟进 Excel VO
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

    @ExcelProperty("跟进类型：日常联系、销售线索、催款等")
    private String type;

}
