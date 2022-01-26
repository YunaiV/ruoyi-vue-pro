package cn.iocoder.yudao.adminserver.modules.system.controller.logger.vo.loginlog;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.coreservice.modules.system.enums.SysDictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 登录日志 Excel 导出响应 VO
 */
@Data
public class SysLoginLogExcelVO {

    @ExcelProperty("日志主键")
    private Long id;

    @ExcelProperty("用户账号")
    private String username;

    @ExcelProperty(value = "日志类型", converter = DictConvert.class)
    @DictFormat(SysDictTypeConstants.LOGIN_TYPE)
    private Integer logType;

    @ExcelProperty(value = "登录结果", converter = DictConvert.class)
    @DictFormat(SysDictTypeConstants.LOGIN_RESULT)
    private Integer result;

    @ExcelProperty("登录 IP")
    private String userIp;

    @ExcelProperty("浏览器 UA")
    private String userAgent;

    @ExcelProperty("登录时间")
    private Date createTime;

}
