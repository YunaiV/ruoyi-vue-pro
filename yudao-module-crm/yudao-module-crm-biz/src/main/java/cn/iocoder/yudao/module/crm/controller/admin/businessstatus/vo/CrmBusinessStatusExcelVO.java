package cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

// TODO @lilleo：这个暂时不需要；嘿嘿~不是每个模块都需要导出哈
/**
 * 商机状态 Excel VO
 *
 * @author ljlleo
 */
@Data
public class CrmBusinessStatusExcelVO {

    @ExcelProperty("主键")
    private Long id;

    @ExcelProperty("状态类型编号")
    private Long typeId;

    @ExcelProperty("状态名")
    private String name;

    @ExcelProperty("赢单率")
    private String percent;

    @ExcelProperty("排序")
    private Integer sort;

}
