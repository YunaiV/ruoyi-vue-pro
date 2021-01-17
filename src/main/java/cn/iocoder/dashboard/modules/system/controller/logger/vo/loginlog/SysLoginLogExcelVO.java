package cn.iocoder.dashboard.modules.system.controller.logger.vo.loginlog;

import cn.iocoder.dashboard.framework.excel.core.annotations.DictFormat;
import cn.iocoder.dashboard.framework.excel.core.convert.DictConvert;
import cn.iocoder.dashboard.modules.system.enums.dict.DictTypeEnum;
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

    @ExcelProperty("用户编号")
    private Long userId;

    @ExcelProperty("用户账号")
    private String username;

    @ExcelProperty(value = "登陆结果", converter = DictConvert.class)
    @DictFormat(DictTypeEnum.SYS_LOGIN_RESULT)
    private Integer result;

    @ExcelProperty("登陆 IP")
    private String userIp;

    @ExcelProperty("浏览器 UA")
    private String userAgent;

    @ExcelProperty("登陆时间")
    private Date createTime;

}
