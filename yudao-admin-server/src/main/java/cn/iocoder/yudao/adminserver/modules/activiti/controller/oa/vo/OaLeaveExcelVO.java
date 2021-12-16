package cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 请假申请 Excel VO
 *
 * @author 芋艿
 */
@Data
public class OaLeaveExcelVO {

    @ExcelProperty("请假表单主键")
    private Long id;

    @ExcelProperty("流程id")
    private String processInstanceId;

    @ExcelProperty("状态")
    private Integer status;

    @ExcelProperty("申请人id")
    private String userId;

    @ExcelProperty("开始时间")
    private Date startTime;

    @ExcelProperty("结束时间")
    private Date endTime;

    @ExcelProperty("请假类型")
    private String leaveType;

    @ExcelProperty("原因")
    private String reason;

    @ExcelProperty("申请时间")
    private Date applyTime;

}
