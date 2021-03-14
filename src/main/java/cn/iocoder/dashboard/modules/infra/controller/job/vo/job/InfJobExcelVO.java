package cn.iocoder.dashboard.modules.infra.controller.job.vo.job;

import cn.iocoder.dashboard.framework.excel.core.annotations.DictFormat;
import cn.iocoder.dashboard.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

import static cn.iocoder.dashboard.modules.system.enums.dict.SysDictTypeEnum.INF_JOB_STATUS;

/**
 * 定时任务 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class InfJobExcelVO {

    @ExcelProperty("任务编号")
    private Long id;

    @ExcelProperty("任务名称")
    private String name;

    @ExcelProperty(value = "任务状态", converter = DictConvert.class)
    @DictFormat(INF_JOB_STATUS)
    private Integer status;

    @ExcelProperty("处理器的名字")
    private String handlerName;

    @ExcelProperty("处理器的参数")
    private String handlerParam;

    @ExcelProperty("CRON 表达式")
    private String cronExpression;

    @ExcelProperty("最后一次执行的开始时间")
    private Date executeBeginTime;

    @ExcelProperty("最后一次执行的结束时间")
    private Date executeEndTime;

    @ExcelProperty("上一次触发时间")
    private Date firePrevTime;

    @ExcelProperty("下一次触发时间")
    private Date fireNextTime;

    @ExcelProperty("监控超时时间")
    private Integer monitorTimeout;

    @ExcelProperty("创建时间")
    private Date createTime;

}
