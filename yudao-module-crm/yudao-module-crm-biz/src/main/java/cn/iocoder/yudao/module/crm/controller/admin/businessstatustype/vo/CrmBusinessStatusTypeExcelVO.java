package cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

// TODO @lilleo：这个暂时不需要；嘿嘿~不是每个模块都需要导出哈
/**
 * 商机状态类型 Excel VO
 *
 * @author ljlleo
 */
@Data
public class CrmBusinessStatusTypeExcelVO {

    @ExcelProperty("主键")
    private Long id;

    @ExcelProperty("状态类型名")
    private String name;

    @ExcelProperty("使用的部门编号")
    private String deptIds;

    @ExcelProperty("开启状态")
    private Boolean status;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
