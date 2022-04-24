package cn.iocoder.yudao.module.infra.controller.admin.logger.vo.apiaccesslog;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * API 访问日志 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class ApiAccessLogExcelVO {

    @ExcelProperty("日志主键")
    private Long id;

    @ExcelProperty("链路追踪编号")
    private String traceId;

    @ExcelProperty("用户编号")
    private Long userId;

    @ExcelProperty(value = "用户类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.USER_TYPE)
    private Integer userType;

    @ExcelProperty("应用名")
    private String applicationName;

    @ExcelProperty("请求方法名")
    private String requestMethod;

    @ExcelProperty("请求地址")
    private String requestUrl;

    @ExcelProperty("请求参数")
    private String requestParams;

    @ExcelProperty("用户 IP")
    private String userIp;

    @ExcelProperty("浏览器 UA")
    private String userAgent;

    @ExcelProperty("开始请求时间")
    private Date beginTime;

    @ExcelProperty("结束请求时间")
    private Date endTime;

    @ExcelProperty("执行时长")
    private Integer duration;

    @ExcelProperty("结果码")
    private Integer resultCode;

    @ExcelProperty("结果提示")
    private String resultMsg;

}
