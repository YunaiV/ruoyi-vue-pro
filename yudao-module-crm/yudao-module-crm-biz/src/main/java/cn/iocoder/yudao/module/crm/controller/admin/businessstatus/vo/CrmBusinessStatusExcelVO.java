package cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;

import com.alibaba.excel.annotation.ExcelProperty;

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
