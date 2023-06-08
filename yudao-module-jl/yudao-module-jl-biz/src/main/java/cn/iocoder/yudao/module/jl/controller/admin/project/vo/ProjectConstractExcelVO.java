package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 项目合同 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class ProjectConstractExcelVO {

    @ExcelProperty("岗位ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("项目 id")
    private Long projectId;

    @ExcelProperty("合同名字")
    private String name;

    @ExcelProperty("合同文件 URL")
    private String fileUrl;

    @ExcelProperty("合同状态：起效、失效、其它")
    private String status;

    @ExcelProperty("合同类型")
    private String type;

    @ExcelProperty("合同金额")
    private Long price;

    @ExcelProperty("签订销售人员")
    private Long salesId;

    @ExcelProperty("合同编号")
    private String sn;

    @ExcelProperty("合同文件名")
    private String fileName;

}
