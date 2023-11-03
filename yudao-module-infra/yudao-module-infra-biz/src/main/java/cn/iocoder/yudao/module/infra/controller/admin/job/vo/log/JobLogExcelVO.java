package cn.iocoder.yudao.module.infra.controller.admin.job.vo.log;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.infra.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务 Excel VO
 *
 * @author 芋艿
 */
@Data
public class JobLogExcelVO {

    @ExcelProperty("日志编号")
    private Long id;

    @ExcelProperty("任务编号")
    private Long jobId;

    @ExcelProperty("处理器的名字")
    private String handlerName;

    @ExcelProperty("处理器的参数")
    private String handlerParam;

    @ExcelProperty("第几次执行")
    private Integer executeIndex;

    @ExcelProperty("开始执行时间")
    private LocalDateTime beginTime;

    @ExcelProperty("结束执行时间")
    private LocalDateTime endTime;

    @ExcelProperty("执行时长")
    private Integer duration;

    @ExcelProperty(value = "任务状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.JOB_STATUS)
    private Integer status;

    @ExcelProperty("结果数据")
    private String result;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
