package cn.iocoder.yudao.module.infra.controller.admin.demo02.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 学生 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class InfraDemoStudentExcelVO {

    @ExcelProperty("编号")
    private Long id;

}