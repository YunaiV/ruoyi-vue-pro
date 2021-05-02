package cn.iocoder.yudao.adminserver.modules.system.controller.logger.vo.loginlog;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.adminserver.modules.system.enums.SysDictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 登陆日志 Excel 导出响应 VO
 */
@Data
public class SysLoginLogExcelVO {

    @ExcelProperty("日志主键")
    private Long id;

    @ExcelProperty("用户账号")
    private String username;

    @ExcelProperty(value = "登陆结果", converter = DictConvert.class)
    @DictFormat(SysDictTypeConstants.LOGIN_RESULT)
    private Integer result;

    @ExcelProperty("登陆 IP")
    private String userIp;

    @ExcelProperty("浏览器 UA")
    private String userAgent;

    @ExcelProperty("登陆时间")
    private Date createTime;

}
